#!/bin/bash
BIN_PATH=`dirname "$0"`
INSTANCE_NAME=$2
RPC_PORT=$3

if [ -z "$INSTANCE_NAME" -o -z "$RPC_PORT" ]; then
    echo "usage: ./run-rpc.sh [start|stop|restart|list] [INSTANCE_NAME] [RPC_PORT]"
    exit 1
fi

source $BIN_PATH/config.sh

JVMOPTS="-server -Xms64m -Xmx2g -XX:NewSize=512m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-Dmode=rpc"

source $BIN_PATH/common.sh