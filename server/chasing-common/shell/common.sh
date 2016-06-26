#!/bin/bash
if [ -z "$SERVICE_NAME" -o -z "$MODE" ]; then
    echo "service_name or mode is null"
    exit 1
fi

cd $PROJECT_HOME
JVMARGS_DEFAULT="-Dlogpath=$LOG_PATH -Dlogback.configurationFile=$CONFIG_PATH/logback.xml -Dfile.encoding=UTF-8"

case $1 in
    start)
    echo "Starting $SERVICE_NAME ..."
    if [ ! -f $PID_FILE ]; then
        nohup java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS $MODE -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
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
        nohup java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS $MODE -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$SERVICE_NAME started"
    else
        echo "$SERVICE_NAME is not running"
        nohup java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS $MOD -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$SERVICE_NAME started"
    fi
    ;;
esac                      