#!/bin/bash
BIN_PATH=`dirname "$0"`

SERVICE_NAME=cron-service
MOD="-Dmod=cron"
JVMOPTS="-server -Xms1g -Xmx2g -XX:NewSize=512m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"                                                    

source $BIN_PATH/common.sh