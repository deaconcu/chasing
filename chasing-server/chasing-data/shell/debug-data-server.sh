#!/bin/bash
BIN_PATH=`dirname "$0"`
INSTANCE_NAME=$2
RPC_PORT=$3
MODE=data-server

source $BIN_PATH/config.sh

JVMOPTS="-server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$4"
JVMARGS="-Dmode=$MODE -Drpc.port=$RPC_PORT"

source $BIN_PATH/common.sh