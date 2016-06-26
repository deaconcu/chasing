#!/bin/bash
BIN_PATH=`dirname "$0"`
INSTANCE_NAME=$2
HTTP_PORT=$3
MODE=api-server

source $BIN_PATH/config.sh

JVMOPTS="-server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$4"
JVMARGS="-Dmode=$MODE -Dserver.port=$HTTP_PORT"

source $BIN_PATH/common.sh