#!/usr/bin/env bash
# Blocks until Argo CD has reconciled the cluster to a specific Git commit.
#
# Waiting on "Synced" alone is not enough: Argo may still be reporting the
# PREVIOUS revision as synced when this runs, which would let a smoke test pass
# against the old workload and report a success that never happened. The check
# below therefore requires the observed revision to match the commit we pushed
# before it accepts Synced/Healthy.
set -euo pipefail

REVISION="${1:-}"
APP="${ARGO_APP:-ead-platform}"
ARGO_NS="${ARGO_NS:-argocd}"
TIMEOUT="${TIMEOUT:-600}"

if [[ -z "$REVISION" ]]; then
  echo "usage: $0 <git-sha>" >&2
  exit 2
fi

echo "==> waiting for Argo CD app '${APP}' to reach ${REVISION:0:8}"

# Argo polls Git every few minutes by default; ask it to look now so the
# pipeline is not waiting on a timer.
kubectl -n "$ARGO_NS" annotate application "$APP" \
  argocd.argoproj.io/refresh=hard --overwrite >/dev/null

deadline=$((SECONDS + TIMEOUT))
while (( SECONDS < deadline )); do
  observed=$(kubectl -n "$ARGO_NS" get application "$APP" \
    -o jsonpath='{.status.sync.revision}' 2>/dev/null || echo "")
  sync=$(kubectl -n "$ARGO_NS" get application "$APP" \
    -o jsonpath='{.status.sync.status}' 2>/dev/null || echo "")
  health=$(kubectl -n "$ARGO_NS" get application "$APP" \
    -o jsonpath='{.status.health.status}' 2>/dev/null || echo "")

  printf 'revision=%s sync=%s health=%s\n' "${observed:0:8}" "$sync" "$health"

  if [[ "$observed" == "$REVISION" && "$sync" == "Synced" && "$health" == "Healthy" ]]; then
    echo "==> Argo CD converged on ${REVISION:0:8}"
    exit 0
  fi

  if [[ "$health" == "Degraded" ]]; then
    echo "::error::Argo CD reports the application as Degraded" >&2
    kubectl -n "$ARGO_NS" get application "$APP" -o yaml >&2
    exit 1
  fi

  sleep 10
done

echo "::error::timed out after ${TIMEOUT}s waiting for ${REVISION:0:8}" >&2
kubectl -n "$ARGO_NS" get application "$APP" -o yaml >&2
exit 1
