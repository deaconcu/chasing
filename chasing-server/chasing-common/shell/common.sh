#!/bin/bash
if [ -z "$INSTANCE_NAME" ]; then
    echo "instance_name is not provided"
    exit 1
fi

cd $PROJECT_HOME
CLASSPATH="$LIB_PATH/*"
JVMARGS_DEFAULT="-Dlogpath=$LOG_PATH -Dlogback.configurationFile=$CONFIG_PATH/logback.xml -Dfile.encoding=UTF-8"

case $1 in
    start)
    echo "Starting $INSTANCE_NAME ..."
    if [ ! -f $PID_FILE ]; then
        echo "execute command:"
        echo "java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &" 
        nohup java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$INSTANCE_NAME started"
    else
        echo "$INSTANCE_NAME is already running"
    fi
    ;;  
    stop)
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE);
        echo "$INSTANCE_NAME stoping ..."
        kill -9 $PID;
        echo "$INSTANCE_NAME stopped"
        rm $PID_FILE
    else
        echo "$INSTANCE_NAME is not running ..."
    fi
    ;;
    restart)
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE);
        echo "$INSTANCE_NAME stopping ...";
        kill -9 $PID;
        echo "$INSTANCE_NAME stopped";
        rm $PID_FILE
        echo "$INSTANCE_NAME starting ..."
        echo "execute command:"
        echo "java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &" 
        nohup java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$INSTANCE_NAME started"
    else
        echo "$INSTANCE_NAME is not running"
        echo "execute command:"
        echo "java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &" 
        nohup java $JVMOPTS $JVMARGS_DEFAULT $JVMARGS -classpath "$CLASSPATH" $MAIN_CLASS >>$LOG_PATH/system.log 2>&1 &
        echo $! > $PID_FILE
        echo "$INSTANCE_NAME started"
    fi
    ;;
esac                      