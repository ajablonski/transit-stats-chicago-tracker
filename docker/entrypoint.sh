#!/usr/bin/env bash

if [[ -f "/run/secrets/app_bus_tracker_api_key" ]]; then
  export APP_CTA_BUS_API_KEY=$(cat "/run/secrets/app_bus_tracker_api_key")
fi
if [[ -f "/run/secrets/app_train_tracker_api_key" ]]; then
  export APP_CTA_TRAIN_API_KEY=$(cat "/run/secrets/app_train_tracker_api_key")
fi
if [[ -f "/run/secrets/app_secret" ]]; then
  export APP_SECRET=$(cat "/run/secrets/app_secret")
fi

/app/server-0.1.0-SNAPSHOT/bin/server "$@"
#sleep 100000