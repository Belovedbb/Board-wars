FROM alpine:3.10 as packager

MAINTAINER Ayo Beloved <belovedbb1@gmail.com>

ENV JAVA_HOME="/usr/lib/jvm/default-jvm/"
RUN apk add --no-cache bash
RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

ENV PATH=$PATH:${JAVA_HOME}/bin
ADD ./target/v1-delegator.jar /app/

EXPOSE 26349

COPY ./prod-start.sh /app/v1-delegator-start.sh
RUN chmod +x ./app/v1-delegator-start.sh

COPY ./wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

#dependent on public-gateway && kanban-project && management-service && liable scrum coz of kafka dep
CMD ["/bin/sh","-c","/app/wait-for-it.sh public-gateway:8081 -t 0 --strict -- ./app/v1-delegator-start.sh"]




#FROM alpine:3.10 as packager
#
#MAINTAINER Ayo Beloved <belovedbb1@gmail.com>
#
#ENV JAVA_HOME="/usr/lib/jvm/default-jvm/"
#
#RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
#
#ENV PATH=$PATH:${JAVA_HOME}/bin
#
#ADD ./target/v1-delegator.jar /app/
#CMD ["java","-Dspring.profiles.active=prod", "-jar", "/app/v1-delegator.jar"]
#
#EXPOSE 23464
