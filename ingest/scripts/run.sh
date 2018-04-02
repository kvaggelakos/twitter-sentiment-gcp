#/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

set -o allexport
source $SCRIPTPATH/../../common/env
set +o allexport

GOOGLE_APPLICATION_CREDENTIALS=$SCRIPTPATH/../../common/credentials.json \
  python $SCRIPTPATH/../main.py \
  --twitter_topcis bitcoin,litecoin,ethereum