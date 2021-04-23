#!/usr/bin/env bash
set -eo pipefail

export APP_CTA_BUS_API_KEY=$(cat "/run/secrets/app_bus_tracker_api_key")
export APP_CTA_TRAIN_API_KEY=$(cat "/run/secrets/app_train_tracker_api_key")
export APP_SECRET=$(cat "/run/secrets/app_secret")

# shellcheck disable=SC2206
HOSTS_ARR=(localhost $HOSTS)
for i in "${!HOSTS_ARR[@]}" ; do
    HOST_ARGS="$HOST_ARGS -Dplay.filters.hosts.allowed.$i=${HOSTS_ARR[$i]}"
done

echo $HOST_ARGS
echo "$@"

# shellcheck disable=SC2086
/app/server-0.1.0-SNAPSHOT/bin/server $HOST_ARGS "$@"
