#!/bin/sh

# shellcheck disable=SC2046
git pull origin $(git rev-parse --abbrev-ref HEAD)

docker-compose down
mvn clean package -Dmaven.test.skip=true