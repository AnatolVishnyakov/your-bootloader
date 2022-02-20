#!/bin/sh
docker-compose down
docker kill $(docker ps -q)