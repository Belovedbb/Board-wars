FROM alpine:3.10 as packager

MAINTAINER Ayo Beloved <belovedbb1@gmail.com>

ENV JAVA_HOME="/usr/lib/jvm/default-jvm/"
RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

ENV PATH=$PATH:${JAVA_HOME}/bin

ENV SPRING_PROFILES_ACTIVE default

ADD ./target/config.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/config.jar"]
RUN apk add --update  curl   && rm -rf /var/cache/apk/*
HEALTHCHECK --interval=30s --timeout=3000s CMD curl -f http://localhost:8071/actuator/health || exit 1

EXPOSE 8071


#FROM alpine:latest as packager
#
#MAINTAINER Ayo Beloved <belovedbb1@gmail.com>
#
#RUN apk --no-cache add openjdk11-jdk openjdk11-jmods
#
#ENV JAVA_MINIMAL="/opt/java-minimal"
#
#RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
#    --verbose \
#    --add-modules \
#        java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.xml,java.logging \
#    --compress 2 --strip-debug --no-header-files --no-man-pages \
#    --release-info="add:IMPLEMENTOR=radistao:IMPLEMENTOR_VERSION=radistao_JRE" \
#    --output "$JAVA_MINIMAL"
#
#FROM alpine:latest
#
#ENV JAVA_HOME=/opt/java-minimal
#ENV PATH="$PATH:$JAVA_HOME/bin"
#
#COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
#
#ADD ./target/config.jar /app/
#CMD ["java", "-Xmx200m", "-jar", "/app/config.jar"]
#
#HEALTHCHECK --interval=30s --timeout=30s CMD curl -f http://localhost:8071/actuator/health || exit 1
#
#EXPOSE 8071
#
#

#FROM alpine:latest as packager
#
#MAINTAINER Ayo Beloved <belovedbb1@gmail.com>
##openjdk11-jmods
#RUN apk --no-cache add openjdk11-jdk  --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
#
#ENV JAVA_MINIMAL="/opt/java-minimal"
#
#RUN /usr/lib/jvm/java-11-openjdk/bin/jlink \
#    --verbose \
##    --add-modules \
##        java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,com.sun.management \
#    --compress 2 --strip-debug --no-header-files --no-man-pages \
#    --release-info="add:IMPLEMENTOR=radistao:IMPLEMENTOR_VERSION=radistao_JRE" \
#    --output "$JAVA_MINIMAL"
#
#FROM alpine:latest
#
#ENV JAVA_HOME=/opt/java-minimal
#ENV PATH="$PATH:$JAVA_HOME/bin"
#
#COPY --from=packager "$JAVA_HOME" "$JAVA_HOME"
#
#ADD ./target/config.jar /app/
#CMD ["java", "-Xmx200m", "-jar", "/app/config.jar"]
#
#HEALTHCHECK --interval=30s --timeout=30s CMD curl -f http://localhost:8071/actuator/health || exit 1
#
#EXPOSE 8071
