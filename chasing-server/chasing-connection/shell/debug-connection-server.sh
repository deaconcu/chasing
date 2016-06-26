#!/bin/bash
BIN_PATH=`dirname "$0"`
INSTANCE_NAME=$2
RPC_PORT=$3
UDP_PORT=$4
MODE=connection-server

source $BIN_PATH/config.sh

JVMOPTS="-server -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$5"
JVMARGS="-Dmode=$MODE -Drpc.port=$RPC_PORT -Dudp.port=$UDP_PORT"

source $BIN_PATH/common.sh