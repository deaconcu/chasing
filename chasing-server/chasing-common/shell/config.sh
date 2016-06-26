#!/bin/bash
BIN_PATH=`dirname "$0"`
CURR_HOME=`pwd`
cd $BIN_PATH
cd ..
PACKAGE_HOME=`pwd`
cd ..
PROJECT_HOME=`pwd`

PID_PATH=$PROJECT_HOME/pid
if [ ! -d $PID_PATH ]; then
    mkdir $PID_PATH
fi

PID_FILE=$PID_PATH/$SERVICE_NAME.pid
LOG_PATH=$PROJECT_HOME/log
CONFIG_PATH=$PROJECT_HOME/config
LIB_PATH=$PACKAGE_HOME/lib
MAIN_CLASS=com.youku.java.raptor.boot.Boot

cd $CURR_HOME