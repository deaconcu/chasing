#!/bin/bash
BIN_PATH=`dirname "$0"`
INSTANCE_NAME=$2
RPC_PORT=$3
MODE=data-server

source $BIN_PATH/config.sh

JVMOPTS="-server -Xms64m -Xmx2g -XX:NewSize=512m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-Dmode=$MODE -Drpc.port=$RPC_PORT"

source $BIN_PATH/common.sh