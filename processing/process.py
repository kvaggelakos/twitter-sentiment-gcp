from __future__ import absolute_import

import argparse
import logging
import re

import six
from os import path
import os
import json
import time

# Cloud Dataflow stuff
import apache_beam as beam
from apache_beam.io.gcp.bigquery import parse_table_schema_from_json
from apache_beam.io import ReadFromText, WriteToText, ReadStringsFromPubSub
from apache_beam.metrics import Metrics
from apache_beam.options.pipeline_options import PipelineOptions, SetupOptions
from apache_beam.testing.util import assert_that, equal_to

# NLP stuff
from google.cloud import language
from google.cloud.language import enums
from google.cloud.language import types


class SentimentPredictionFn(beam.DoFn):
  """A DoFn that runs sentiment on the body of the tweet"""

  def start_bundle(self):
    self._ls_client = language.LanguageServiceClient()

  def process(self, element):
    try:
      document = types.Document(content=element['extended_tweet']['full_text'], type=enums.Document.Type.PLAIN_TEXT)
      sentiment = self._ls_client.analyze_sentiment(document=document).document_sentiment
      element['sentiment'] = {
        'score': sentiment.score,
        'magnitude': sentiment.magnitude
      }
    except:
      element['sentiment'] = {'score': 0, 'magnitude': 0}
    yield element


class ExtractTweetData(beam.DoFn):

  def __init__(self, schema):
    super(ExtractTweetData, self).__init__()
    self.schema = json.loads(schema)

  def _parse_schema_field(self, field, element):
    key = field['name']

    # An ugly way to pick out data with ifs
    if not key in element:
      return None

    val = element[key]
    if 'fields' in field:
      return {x['name']: self._parse_schema_field(x, val) for x in field['fields']}

    if field['type'].lower() == 'timestamp':
      val = time.strftime('%Y-%m-%d %H:%M:%S', time.strptime(val, '%a %b %d %H:%M:%S +0000 %Y'))

    return val

  def process(self, element):
    yield {x['name']: self._parse_schema_field(x, element) for x in self.schema}


class DebugLogger(beam.DoFn):
  def process(self, element):
    print(element)
    yield element


def run(argv=None):
  parser = argparse.ArgumentParser()

  # General

  # Local
  parser.add_argument('--input-file',
                      dest='input_file',
                      default=path.join(path.realpath(__file__), './samples/tweet.json'),
                      help='Specify the input file')
  parser.add_argument('--output-file',
                      dest='output_file',
                      default=path.join(path.realpath(__file__), './tmp/result.csv'),
                      help='Specify the output file')

  # Google cloud
  parser.add_argument('--topic',
                      dest='topic',
                      help='The cloud pub sub topic to read from')
  parser.add_argument('--schema',
                      dest='schema',
                      help='Where to read in the schema file from')
  parser.add_argument('--bigquery-table',
                      dest='bq_table',
                      help='Output BigQuery table for results specified as: PROJECT:DATASET.TABLE or DATASET.TABLE.')

  known_args, pipeline_args = parser.parse_known_args(argv)


  # We use the save_main_session option because one or more DoFn's in this
  # workflow rely on global context (e.g., a module imported at module level).
  pipeline_options = PipelineOptions(pipeline_args)
  pipeline_options.view_as(SetupOptions).save_main_session = True
  with beam.Pipeline(options=pipeline_options) as p:

    if known_args.topic:

      with open(known_args.schema, 'r') as schema_file:
        schema = schema_file.read()

      (p  | 'Read from pubsub' >> beam.io.ReadStringsFromPubSub(known_args.topic)
          | 'Load into JSON' >> beam.Map(json.loads)
          | 'Filter out original tweets only' >> beam.Filter(lambda row: not any((row.get("retweeted_status"), row.get("in_reply_to_status_id_str"), row.get("in_reply_to_user_id_str"))))
          | 'Select important data' >> beam.ParDo(ExtractTweetData(schema))
          | 'Sentiment analysis' >> beam.ParDo(SentimentPredictionFn())
          | 'Write to BQ' >> beam.io.WriteToBigQuery(
              known_args.bq_table,
              create_disposition=beam.io.BigQueryDisposition.CREATE_IF_NEEDED,
              write_disposition=beam.io.BigQueryDisposition.WRITE_APPEND)
      )
    else:
      # run locally with file
      pass


if __name__ == '__main__':
  logging.getLogger().setLevel(logging.INFO)
  run()