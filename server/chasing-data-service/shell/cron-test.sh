#!/bin/bash
BIN_PATH=`dirname "$0"`

SERVICE_NAME=cron-service
MOD="-Dmod=cron"
JVMOPTS="-server"                                                    

source $BIN_PATH/common.sh