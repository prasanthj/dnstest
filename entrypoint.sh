#!/bin/bash

cd $JAVA_HOME/lib/security/
sed -i -e 's/^networkaddress.cache.ttl=.*/networkaddress.cache.ttl=0/'  java.security
cd -

java -cp /dnstest-1.0-SNAPSHOT-jar-with-dependencies.jar $MAIN_CLASS
