#!/usr/bin/env bash
# Restores a mongodump archive produced by backup-mongodb.sh.
#
# --drop makes the restore idempotent: replaying the same archive twice yields
# the same state, which is what makes recovery drills safe to rehearse.
set -euo pipefail

NAMESPACE="${NAMESPACE:-ead-platform}"
POD="${POD:-mongodb-0}"
ARCHIVE="${1:-}"

if [[ -z "$ARCHIVE" || ! -f "$ARCHIVE" ]]; then
  echo "usage: $0 <path-to-archive.gz>" >&2
  exit 2
fi

echo "==> reading credentials from the Kubernetes Secret"
USERNAME=$(kubectl -n "$NAMESPACE" get secret mongodb-credentials -o jsonpath='{.data.username}' | base64 -d)
PASSWORD=$(kubectl -n "$NAMESPACE" get secret mongodb-credentials -o jsonpath='{.data.password}' | base64 -d)

echo "==> restoring ${ARCHIVE} into ${POD}"
kubectl -n "$NAMESPACE" exec -i "$POD" -- \
  mongorestore --username="$USERNAME" --password="$PASSWORD" \
    --authenticationDatabase=admin --archive --gzip --drop \
  < "$ARCHIVE"

echo "==> verifying document counts"
kubectl -n "$NAMESPACE" exec "$POD" -- mongosh --quiet \
  --username="$USERNAME" --password="$PASSWORD" --authenticationDatabase=admin \
  --eval '["recipedb","userdb","reviewdb"].forEach(d => { const c = db.getSiblingDB(d); c.getCollectionNames().forEach(n => print(d + "." + n + " = " + c.getCollection(n).countDocuments())); })'

echo "restore complete"
