#!/bin/bash

IPC_LOG_LEVEL=${IPC_LOG_LEVEL:-WARN}
LOG_LEVEL=${LOG_LEVEL:-WARN}
if [[ -z "${DISABLE_JVM_DNS_CACHING}" ]]; then
  echo "Using default JVM DNS caching values.."
else
  echo "Disabling JVM DNS caching.."
  cd $JAVA_HOME/lib/security/ || exit
  sed -i -e 's/^networkaddress.cache.ttl=.*/networkaddress.cache.ttl=0/'  java.security
  cd - || exit
fi

java -cp /dnstest-1.0-SNAPSHOT-jar-with-dependencies.jar -Dlog4j.ipcLogLevel="${IPC_LOG_LEVEL}" -Dlog4j.logLevel="${LOG_LEVEL}" "$MAIN_CLASS"
