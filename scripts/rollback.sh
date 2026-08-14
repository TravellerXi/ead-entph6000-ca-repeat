#!/usr/bin/env bash
# Demonstrates the two rollback paths the platform supports.
#
#   rolling   -> kubectl rollout undo, for services released with RollingUpdate
#   bluegreen -> switch the Service selector back to the previous slot
#   helm      -> helm rollback, which reverts every object in the release at once
#
# BREAK-GLASS ONLY. Argo CD owns this namespace with selfHeal enabled, so every
# mode below is reverted within seconds unless auto-sync is paused first:
#
#   kubectl -n argocd patch application ead-platform --type merge \
#     -p '{"spec":{"syncPolicy":{"automated":null}}}'
#
# The routine rollback path is the CD workflow's revert job, which puts the
# previous desired state back into Git so the change survives reconciliation.
set -euo pipefail

NAMESPACE="${NAMESPACE:-ead-platform}"
RELEASE="${RELEASE:-ead}"
MODE="${1:-}"

case "$MODE" in
  rolling)
    DEPLOYMENT="${2:-recipe-service}"
    echo "==> revision history for ${DEPLOYMENT}"
    kubectl -n "$NAMESPACE" rollout history "deployment/${DEPLOYMENT}"
    echo "==> undoing last rollout"
    kubectl -n "$NAMESPACE" rollout undo "deployment/${DEPLOYMENT}"
    kubectl -n "$NAMESPACE" rollout status "deployment/${DEPLOYMENT}" --timeout=5m
    kubectl -n "$NAMESPACE" rollout history "deployment/${DEPLOYMENT}"
    ;;

  bluegreen)
    current=$(kubectl -n "$NAMESPACE" get svc review-service \
      -o jsonpath='{.metadata.annotations.ead\.tudublin\.ie/active-colour}')
    previous=$([[ "$current" == "blue" ]] && echo green || echo blue)
    echo "==> active is ${current}; reverting to ${previous}"
    exec "$(dirname "$0")/switch-colour.sh" "$previous"
    ;;

  helm)
    echo "==> release history"
    helm history "$RELEASE" --namespace "$NAMESPACE"
    echo "==> rolling back one revision"
    helm rollback "$RELEASE" --namespace "$NAMESPACE" --wait --timeout 5m
    helm history "$RELEASE" --namespace "$NAMESPACE"
    ;;

  *)
    echo "usage: $0 <rolling [deployment]|bluegreen|helm>" >&2
    exit 2
    ;;
esac
