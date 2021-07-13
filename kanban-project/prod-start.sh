#!/usr/bin/env bash

./app/wait-for-it.sh public-auth:9000  -t 0  --strict -- echo "STARTING APPLICATION KANBAN PROJECT .jar..." && \
 java -Xmx200m  -Dspring.profiles.active=prod -jar /app/kanban-project.jar
