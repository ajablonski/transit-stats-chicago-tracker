#!/usr/bin/env bash
set -eo pipefail

# shellcheck disable=SC2086
/app/transit-stats-chicago-server-0.1.0-SNAPSHOT/bin/transit-stats-chicago-server "$@"
