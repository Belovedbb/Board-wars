#!/usr/bin/env bash

./app/wait-for-it.sh public-auth:9000  -t 0  --strict -- echo "STARTING APPLICATION MANAGEMENTT SERVICE .jar..." && \
 java -Xmx200m  -Dspring.profiles.active=prod -jar /app/management-service.jar
