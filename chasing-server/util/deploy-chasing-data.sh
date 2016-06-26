#!/bin/bash
X_HOME=`dirname "$0"`
VERSION=$1
if [ -z "$VERSION" ]; then
    echo "version is not provided"
    exit 1
fi

PROJECT_NAME=chasing-data
PID_DIR=run
CONFIG_DIR=etc
LOG_DIR=log
BIN_DIR=bin
LIB_DIR=lib

cd $X_HOME
cd ..
if [ ! -d $PID_DIR ]; then
    mkdir $PID_DIR
fi

if [ ! -d $CONFIG_DIR ]; then
    mkdir $CONFIG_DIR
fi

if [ ! -d $LOG_DIR ]; then
    mkdir $LOG_DIR
fi


echo "get package ..."
wget http://120.27.112.99:8081/artifactory/libs-release-local/com/prosper/$PROJECT_NAME/$VERSION-release/$PROJECT_NAME-$VERSION-release.tgz

PACKAGE_NAME=$PROJECT_NAME-$VERSION-release.tgz
if [ ! -f $PACKAGE_NAME ]; then
    echo "get package failed, exit";
    exit 1
fi

echo "deal with package ..."
if [ -d $BIN_DIR ]; then
    rm -rf $BIN_DIR
fi
if [ -d $LIB_DIR ]; then
    rm -rf $LIB_DIR
fi
tar xzf $PACKAGE_NAME --strip-components=1 
rm -f $PACKAGE_NAME

chmod +x $BIN_DIR/*

echo "start application ..."