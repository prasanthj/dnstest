FROM base-centos-jdk:latest

ENV MAIN_CLASS="HttpPingServer"
COPY target/dnstest-1.0-SNAPSHOT-jar-with-dependencies.jar /
COPY entrypoint.sh /
COPY healthz.sh /
ENTRYPOINT ["/entrypoint.sh"]
