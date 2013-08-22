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

java $DEBUG -jar $JAR $*
