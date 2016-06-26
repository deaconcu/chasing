#!/bin/bash
BIN_PATH=`dirname "$0"`
SERVICE_NAME=http-service
source $BIN_PATH/config.sh

MODE="-Dmode=http"
JVMOPTS="-server -Xms1g -Xmx2g -XX:NewSize=512m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-Dlog4j.configuration=file://$CONFIG_PATH/log4j.properties"
CLASSPATH="$LIB_PATH/*"

CUSTOM_FILE=$BIN_PATH/http-custom.sh
if [ -e $CUSTOM_FILE ]; then
    source $CUSTOM_FILE
fi
source $BIN_PATH/common.sh