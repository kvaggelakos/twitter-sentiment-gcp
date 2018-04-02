import json
import os
import sys
import argparse

from tweepy import StreamListener, OAuthHandler, Stream

from google.cloud.pubsub import types
from google.cloud import pubsub

from dotenv import load_dotenv, find_dotenv
load_dotenv(find_dotenv())

class PubSubListener(StreamListener):

    def __init__(self):
        super(StreamListener, self).__init__()
        self.publisher = pubsub.PublisherClient(
            batch_settings=types.BatchSettings(max_messages=500)
        )

    def on_data(self, data):
        tweet = json.loads(data)
        # Only publish original tweets
        if 'extended_tweet' in tweet:
            sys.stdout.write('+')
            self.publisher.publish('projects/{}/topics/{}'.format(
                os.getenv('PROJECT_ID'),
                os.getenv('PUBSUB_TOPIC')
                ),
                data.encode('utf-8')
            )
        else:
            sys.stdout.write('-')
        sys.stdout.flush()
        return True

    def on_error(self, status):
        print status


if __name__ == '__main__':

    parser = argparse.ArgumentParser()

    parser.add_argument('--twitter-topics',
                      dest='twitter_topics',
                      default='bitcoin',
                      help='A comma separated list of topics to track')

    known_args, _ = parser.parse_known_args(sys.argv)

    #This handles Twitter authetification and the connection to Twitter Streaming API
    auth = OAuthHandler(os.getenv('TWITTER_CONSUMER_KEY'), os.getenv('TWITTER_CONSUMER_SECRET'))
    auth.set_access_token(os.getenv('TWITTER_ACCESS_TOKEN'), os.getenv('TWITTER_ACCESS_SECRET'))
    stream = Stream(auth=auth, listener=PubSubListener(), tweet_mode='extended')

    #This line filter Twitter Streams to capture data by the keywords: 'python', 'javascript', 'ruby'
    stream.filter(track=known_args.twitter_topics.split(','), languages=['en'])