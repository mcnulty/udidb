#!/bin/bash
#
# Script to launch a development build of udidb
#
# It assumes that udidb has been packaged as a uber-jar
#

JAR=target/udidb-1.0-SNAPSHOT.jar
DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

if [ ! -e $JAR ]; then
    echo "udidb jar does not exist. Run mvn package"
    exit 1
fi

if [ -z ${UDI_LIB_DIR} ]; then
    echo "UDI_LIB_DIR must be set"
    exit 1
fi

if [ -z ${UDI_RT_LIB_DIR} ]; then
    echo "UDI_RT_LIB_DIR must be set"
    exit 1
fi

if [ ! -z ${LD_LIBRARY_PATH} ]; then
    export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${UDI_LIB_DIR}:${UDI_RT_LIB_DIR}
else
    export LD_LIBRARY_PATH=${UDI_LIB_DIR}:${UDI_RT_LIB_DIR}
fi

java $DEBUG -jar $JAR $*
