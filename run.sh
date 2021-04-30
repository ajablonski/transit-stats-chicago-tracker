#!/usr/bin/env bash
set -eo pipefail

sbt dist

cp ./server/target/universal/transit-stats-chicago-server-0.1.0-SNAPSHOT.zip docker/transit-stats-chicago.zip

docker build docker/ -t local/transit-stats-chicago:latest

docker compose up --force-recreate
