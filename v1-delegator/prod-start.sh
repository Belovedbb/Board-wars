#!/usr/bin/env bash

./app/wait-for-it.sh kanban-project:45964  -t 0  --strict  -- ./app/wait-for-it.sh management-service:26349  -t 0 \
 --strict -- echo "STARTING APPLICATION V1 DELEGATOR .jar..." && \
 java -Xmx200m  -Dspring.profiles.active=prod -jar /app/v1-delegator.jar
