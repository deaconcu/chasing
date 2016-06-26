#!/bin/bash                                                                                                                                           
if [ -z "$SERVICE_NAME" -o -z "$MOD" ]; then
    echo "service_name or mod is null"
    exit 1
fi

BIN_PATH=`dirname "$0"`
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
CLASSPATH=$PACKAGE_HOME/lib
MAIN_CLASS=com.youku.java.raptor.runtime.Application

JVMARGS="-Dlogpath=$LOG_PATH -Dlogback.configurationFile=$CONFIG_PATH/logback.xml -Dfile.encoding=UTF-8"

case $1 in
    start)
    echo "Starting $SERVICE_NAME ..."
    if [ ! -f $PID_FILE ]; then
        nohup java $JVMOPTS $JVMARGS $MOD -classpath "$CLASSPATH/*" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$SERVICE_NAME started"
    else
        echo "$SERVICE_NAME is already running"
    fi  
    ;;  
    stop)
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE);
        echo "$SERVICE_NAME stoping ..."
        kill -9 $PID;
        echo "$SERVICE_NAME stopped"
        rm $PID_FILE
    else
        echo "$SERVICE_NAME is not running ..."
    fi
    ;;
    restart)
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE);
        echo "$SERVICE_NAME stopping ...";
        kill -9 $PID;
        echo "$SERVICE_NAME stopped";
        rm $PID_FILE
        echo "$SERVICE_NAME starting ..."
        nohup java $JVMOPTS $JVMARGS $MOD -classpath "$CLASSPATH/*" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$SERVICE_NAME started"
    else
        echo "$SERVICE_NAME is not running"
        nohup java $JVMOPTS $JVMARGS $MOD -classpath "$CLASSPATH/*" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$SERVICE_NAME started"
    fi
    ;;
esac                      