#!/usr/bin/env bash
# Logical backup of every application database to a timestamped archive.
#
# mongodump runs inside the StatefulSet pod, so no database port is exposed
# outside the cluster. The archive is streamed to the operator's machine, which
# keeps the backup off the same disk as the data it protects.
set -euo pipefail

NAMESPACE="${NAMESPACE:-ead-platform}"
POD="${POD:-mongodb-0}"
OUT_DIR="${OUT_DIR:-./backups}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
ARCHIVE="${OUT_DIR}/mongodb-${STAMP}.archive.gz"

mkdir -p "$OUT_DIR"

echo "==> reading credentials from the Kubernetes Secret"
USERNAME=$(kubectl -n "$NAMESPACE" get secret mongodb-credentials -o jsonpath='{.data.username}' | base64 -d)
PASSWORD=$(kubectl -n "$NAMESPACE" get secret mongodb-credentials -o jsonpath='{.data.password}' | base64 -d)

echo "==> dumping all databases from ${POD}"
kubectl -n "$NAMESPACE" exec "$POD" -- \
  mongodump --username="$USERNAME" --password="$PASSWORD" \
    --authenticationDatabase=admin --archive --gzip \
  > "$ARCHIVE"

SIZE=$(wc -c < "$ARCHIVE")
if [[ "$SIZE" -lt 1024 ]]; then
  echo "backup looks empty (${SIZE} bytes) — treating as failure" >&2
  exit 1
fi

echo "==> wrote ${ARCHIVE} (${SIZE} bytes)"
echo "restore with: scripts/restore-mongodb.sh ${ARCHIVE}"
