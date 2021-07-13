FROM alpine:3.10 as packager

MAINTAINER Ayo Beloved <belovedbb1@gmail.com>

ENV JAVA_HOME="/usr/lib/jvm/default-jvm/"

RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

ENV PATH=$PATH:${JAVA_HOME}/bin

ADD ./target/registry.jar /app/
CMD ["java", "-Xmx200m","-Dspring.profiles.active=prod", "-jar", "/app/registry.jar"]

EXPOSE 8888
