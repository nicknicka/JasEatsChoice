#!/usr/bin/env bash
# Block until every listed service reports healthy, or die on timeout.
# Shared by `make up` and CI so there is one definition of "the stack is ready".
#
#   scripts/wait-healthy.sh <service> [<service> ...]
#
# The compose flags come from COMPOSE_FLAGS, e.g.
#   COMPOSE_FLAGS="--env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml"
# so this script never has to re-derive which overlay is in use.
#
# Env: WAIT_TIMEOUT seconds (default 300).
# "exited" fails immediately with the service logs attached. "absent" (container
# not created yet) only counts as pending, so the overall timeout still catches a
# service that never comes up.
set -euo pipefail

TIMEOUT="${WAIT_TIMEOUT:-300}"

if [[ -z "${COMPOSE_FLAGS:-}" ]]; then
  echo "COMPOSE_FLAGS is not set" >&2
  exit 2
fi
if [[ $# -eq 0 ]]; then
  echo "usage: $0 <service> [<service> ...]" >&2
  exit 2
fi

# shellcheck disable=SC2086  # intentional word splitting of the flags
compose() { docker compose $COMPOSE_FLAGS "$@"; }

service_ids() { compose ps -q "$1" 2>/dev/null || true; }

health_of() {
  local ids status running
  ids="$(service_ids "$1")"
  [[ -z "$ids" ]] && { echo "absent"; return; }
  # Check the process state before the health record: a container that crashed
  # keeps reporting its last "unhealthy" snapshot forever, which would otherwise
  # make us wait out the full timeout instead of failing fast.
  running="$(docker inspect --format '{{.State.Running}}' $ids 2>/dev/null | head -1)"
  if [[ "$running" != "true" ]]; then
    echo "exited"
    return
  fi
  status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' $ids 2>/dev/null | head -1)"
  case "$status" in
    none) echo "healthy" ;;  # no healthcheck defined; running is the only signal
    *)    echo "$status" ;;
  esac
}

deadline=$(( $(date +%s) + TIMEOUT ))
waited=""

while true; do
  pending=()
  for svc in "$@"; do
    h="$(health_of "$svc")"
    case "$h" in
      healthy) ;;
      absent)
        # compose has not created this container yet; keep waiting and let the
        # deadline decide.
        pending+=("$svc")
        ;;
      exited)
        echo "FAIL: service '$svc' exited" >&2
        compose logs --tail 40 "$svc" >&2 || true
        exit 1
        ;;
      *) pending+=("$svc") ;;
    esac
    # Log each service once instead of every 5s (bash 3.2 has no assoc arrays).
    # Both wildcards are required: case patterns are anchored, so a trailing "*"
    # is what lets a service match while sitting in the middle of the list.
    case " $waited " in
      *" $svc "* ) ;;
      *) [[ "$h" != healthy ]] && { echo "  waiting on $svc ($h)"; waited="$waited $svc"; } ;;
    esac
  done

  if [[ ${#pending[@]} -eq 0 ]]; then
    echo "all services healthy: $*"
    exit 0
  fi
  if [[ $(date +%s) -ge $deadline ]]; then
    echo "TIMEOUT after ${TIMEOUT}s, still not healthy: ${pending[*]}" >&2
    compose ps >&2 || true
    exit 1
  fi
  sleep 5
done
