#!/bin/bash
BIN_PATH=`dirname "$0"`

SERVICE_NAME=cron-service
MOD="-Dmod=cron"
JVMOPTS="-server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$2"                                                     

source $BIN_PATH/common.sh