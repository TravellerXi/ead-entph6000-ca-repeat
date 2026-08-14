#!/usr/bin/env bash
# Switches the blue/green slot for review-service.
#
# The release is a Service selector change, not a pod rollout: the target slot
# is already running and warm, so cut-over and rollback are both near-instant.
set -euo pipefail

NAMESPACE="${NAMESPACE:-ead-platform}"
RELEASE="${RELEASE:-ead}"
CHART="${CHART:-infrastructure/helm/ead-platform}"
TARGET="${1:-}"

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
kubectl -n "$NAMESPACE" run "smoke-$RANDOM" --rm -i --restart=Never \
  --image=curlimages/curl:8.10.1 -- \
  curl -fsS "http://review-service-${TARGET}:8080/actuator/health/readiness"

echo "==> switching traffic to ${TARGET}"
helm upgrade "$RELEASE" "$CHART" \
  --namespace "$NAMESPACE" \
  --reuse-values \
  -f "${CHART}/values-${TARGET}.yaml" \
  --wait --timeout 5m

echo "==> verifying"
kubectl -n "$NAMESPACE" get svc review-service \
  -o jsonpath='{.metadata.annotations.ead\.tudublin\.ie/active-colour}{"\n"}'
kubectl -n "$NAMESPACE" get endpoints review-service

echo "done. to roll back: $0 ${current}"
