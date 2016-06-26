#!/bin/bash
BIN_PATH=`dirname "$0"`
CURR_HOME=`pwd`
cd $BIN_PATH
cd ..
PROJECT_HOME=`pwd`

PID_PATH=$PROJECT_HOME/run
if [ ! -d $PID_PATH ]; then
    mkdir $PID_PATH
fi

PID_FILE=$PID_PATH/$INSTANCE_NAME.pid
LOG_PATH=$PROJECT_HOME/log
CONFIG_PATH=$PROJECT_HOME/etc
LIB_PATH=$PROJECT_HOME/lib
MAIN_CLASS=com.prosper.chasing.common.boot.Boot

cd $CURR_HOME