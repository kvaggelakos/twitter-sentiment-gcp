#/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

set -o allexport
source $SCRIPTPATH/../../common/env
set +o allexport

# Run locally

GOOGLE_APPLICATION_CREDENTIALS=$SCRIPTPATH/../../common/credentials.json \
python $SCRIPTPATH/../process.py \
  --runner DirectRunner \
  --topic $PUBSUB_TOPIC \
  --schema $SCRIPTPATH/../../common/schema.json \
  --bigquery-table $BQ_TABLE