#!/bin/bash
BIN_PATH=`dirname "$0"`

SERVICE_NAME=http-service
MOD="-Dmod=http"
JVMOPTS="-server"                                                    

source $BIN_PATH/common.sh