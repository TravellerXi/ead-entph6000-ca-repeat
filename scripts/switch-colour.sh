#!/usr/bin/env bash
# Switches the blue/green slot for review-service.
#
# The release is a Service selector change, not a pod rollout: the target slot
# is already running and warm, so cut-over and rollback are both near-instant.
#
# BREAK-GLASS ONLY. Argo CD reconciles this namespace with selfHeal enabled, so
# the switch below is reverted within seconds unless auto-sync is paused first:
#
#   kubectl -n argocd patch application ead-platform --type merge \
#     -p '{"spec":{"syncPolicy":{"automated":null}}}'
#
# Reconcile the incident change into Git before restoring normal control:
#
#   kubectl -n argocd patch application ead-platform --type merge \
#     -p '{"spec":{"syncPolicy":{"automated":{"prune":true,"selfHeal":true}}}}'
#
# The routine path is the CD workflow, which commits activeColour to
# values-live.yaml and lets Argo CD move the selector.
set -euo pipefail

NAMESPACE="${NAMESPACE:-ead-platform}"
TARGET="${1:-}"
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

if [[ "$TARGET" != "blue" && "$TARGET" != "green" ]]; then
  echo "usage: $0 <blue|green>" >&2
  exit 2
fi

current=$(kubectl -n "$NAMESPACE" get svc review-service \
  -o jsonpath='{.metadata.annotations.ead\.tudublin\.ie/active-colour}')
echo "current active colour: ${current}"

if [[ "$current" == "$TARGET" ]]; then
  echo "already serving ${TARGET}; nothing to do"
  exit 0
fi

echo "==> ensuring ${TARGET} slot is ready before switching traffic"
kubectl -n "$NAMESPACE" rollout status "deployment/review-service-${TARGET}" --timeout=5m

echo "==> smoke testing the ${TARGET} slot directly"
kubectl -n "$NAMESPACE" exec -i deployment/frontend -- node - \
  "review-service-${TARGET}" < "${SCRIPT_DIR}/smoke-slot.js"

echo "==> switching traffic to ${TARGET}"
kubectl -n "$NAMESPACE" patch service review-service --type=merge \
  -p "{\"metadata\":{\"annotations\":{\"ead.tudublin.ie/active-colour\":\"${TARGET}\"}},\"spec\":{\"selector\":{\"ead.tudublin.ie/colour\":\"${TARGET}\"}}}"

echo "==> verifying"
kubectl -n "$NAMESPACE" get svc review-service \
  -o jsonpath='{.metadata.annotations.ead\.tudublin\.ie/active-colour}{"\n"}'
kubectl -n "$NAMESPACE" get endpointslice \
  -l kubernetes.io/service-name=review-service

echo "done. to roll back: $0 ${current}"
