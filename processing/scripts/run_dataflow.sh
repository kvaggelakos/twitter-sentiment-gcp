#/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

set -o allexport
source $SCRIPTPATH/../../common/env
set +o allexport


# Run on GCP dataflow

# GOOGLE_APPLICATION_CREDENTIALS=$SCRIPTPATH/../../credentials/credentials.json \
# python $SCRIPTPATH/../process.py \
#   --job_name twitter-sentiment \
#   --runner DataflowRunner \
#   --project $PROJECT_ID \
#   --staging_location gs://$BUCKET_NAME/staging \
#   --temp_location gs://$BUCKET_NAME/temp \
#   --coordinate_output gs://$BUCKET_NAME/out \
#   --grid_size 20 \
#   --topic $PUBSUB_TOPIC \
#   --schema $SCRIPTPATH/../../common/schema.json \
#   --bigquery-table $BQ_TABLE \


echo "Streaming with python is not yet supported :("