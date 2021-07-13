
FROM alpine:3.10 as packager

MAINTAINER Ayo Beloved <belovedbb1@gmail.com>

ENV JAVA_HOME="/usr/lib/jvm/default-jvm/"
RUN apk add --no-cache bash
RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

ENV PATH=$PATH:${JAVA_HOME}/bin
ADD ./target/kanban-project.jar /app/

EXPOSE 45964

COPY ./prod-start.sh /app/kanban-project-start.sh
RUN chmod +x ./app/kanban-project-start.sh

COPY ./wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

#dependent on kafka-server && public-auth
CMD ["/bin/sh","-c","/app/wait-for-it.sh kafka-server:9092 -t 0 --strict -- ./app/kanban-project-start.sh"]

