package org.twitter.sentiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.language.v1beta2.Document;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.twitter.sentiment.models.Sentiment;
import org.twitter.sentiment.models.Tweet;
import org.joda.time.Duration;

public class TwitterSentiment {

  public static void main(String[] args) {

    PipelineOptionsFactory.register(TSPipelineOptions.class);

    TSPipelineOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(TSPipelineOptions.class);

    Pipeline p = Pipeline.create(options);

    p.apply("Read from PubSub", PubsubIO.readMessagesWithAttributes().fromTopic(options.getPubSubTopic()))
        .apply("Parse JSON", ParDo.of(new DoFn<PubsubMessage, Tweet>() {
          @ProcessElement
          public void processElement(ProcessContext c) {
            ObjectMapper mapper = new ObjectMapper();
            try {
              Tweet t = mapper.readValue(new String(c.element().getPayload()), Tweet.class);
              c.output(t);
            } catch (Exception e) {
              System.err.println("Couldn't parse tweet");
            }
          }
        }))
        .apply(Window.<Tweet>into(FixedWindows.of(Duration.standardMinutes(1))))
        .apply("Calculate sentiment score", ParDo.of(new DoFn<Tweet, Tweet>() {

          @ProcessElement
          public void processElement(ProcessContext c) {
            try {

              // TODO: Figure out if cloning makes sense
              Tweet original = c.element();
              Tweet t = original.getClone();

              // Score from Google NLP API
              LanguageServiceClient language = LanguageServiceClient.create();
              Document doc = Document.newBuilder()
                  .setContent(t.extended_tweet.full_text).setType(Document.Type.PLAIN_TEXT).build();
              com.google.cloud.language.v1beta2.Sentiment googleSentiment = language.analyzeSentiment(doc).getDocumentSentiment();

              // Fill in the sentiment information to the tweet model
              Sentiment s = new Sentiment();
              s.magnitude = googleSentiment.getMagnitude();
              s.score = googleSentiment.getScore();
              t.sentiment = s;

              // Output tweet when done
              c.output(t);
            } catch (Exception e) {
              System.err.println("Something went wrong when scoring tweet");
            }
          }
        }))
        .apply("Prepare for BQ", ParDo.of(new DoFn<Tweet, TableRow>() {
          @ProcessElement
          public void processElement(ProcessContext c) {
            Tweet t = c.element();
            c.output(t.toTableRow());
          }
        }))
        .apply(BigQueryIO.writeTableRows()
            .to(options.getBqTable())
            .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
            .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));


    p.run().waitUntilFinish();
  }
}
