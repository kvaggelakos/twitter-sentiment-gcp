#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

set -o allexport
source $SCRIPTPATH/../../common/env
set +o allexport

envsubst < $SCRIPTPATH/../app.template.yaml > $SCRIPTPATH/../app.yaml

gcloud app deploy