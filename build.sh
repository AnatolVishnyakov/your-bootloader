#!/bin/sh

git pull
docker-compose down
mvn clean package -Dmaven.test.skip=true