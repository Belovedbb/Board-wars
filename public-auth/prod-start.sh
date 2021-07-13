#!/usr/bin/env bash

./app/wait-for-it.sh db-server:27017 -t 0  --strict -- echo "Starting application public auth.jar" && \
 java -Xmx200m  -Dspring.profiles.active=prod -jar /app/public-auth-0.0.1.jar
