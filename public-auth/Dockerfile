
FROM alpine:3.10 as packager

MAINTAINER Ayo Beloved <belovedbb1@gmail.com>

ENV JAVA_HOME="/usr/lib/jvm/default-jvm/"
RUN apk add --no-cache bash
RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

ENV PATH=$PATH:${JAVA_HOME}/bin
ADD ./target/public-auth-0.0.1.jar /app/

EXPOSE 9000

COPY ./prod-start.sh /app/public-auth-start.sh
RUN chmod +x ./app/public-auth-start.sh

COPY ./wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

#dependent on registry-server && db-server
CMD ["/bin/sh","-c","/app/wait-for-it.sh registry-server:8888 -t 0 --strict -- ./app/public-auth-start.sh"]
