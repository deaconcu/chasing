#!/bin/bash
BIN_PATH=`dirname "$0"`
INSTANCE_NAME=$2
HTTP_PORT=$3
MODE=api-server

source $BIN_PATH/config.sh

JVMOPTS="-server -Xms64m -Xmx2g -XX:NewSize=512m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-Dmode=$MODE -Dserver.port=$http_PORT"

source $BIN_PATH/common.sh