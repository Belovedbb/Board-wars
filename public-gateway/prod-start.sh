#!/usr/bin/env bash

./app/wait-for-it.sh public-auth:9000 -t 0  --strict -- echo "Starting application public gateway.jar" && \
 java -Xmx200m  -Dspring.profiles.active=prod -jar /app/public-gateway.jar
