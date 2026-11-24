#!/usr/bin/env bash
set -euo pipefail

for attempt in $(seq 1 90); do
    if curl --fail --silent http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
        exit 0
    fi
    sleep 2
done

docker compose logs --no-color api
exit 1
