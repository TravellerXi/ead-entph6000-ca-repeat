#!/usr/bin/env bash
# Creates the Azure Storage account that holds Terraform remote state.
#
# This runs ONCE, by hand, before the first `terraform apply`, and deliberately
# lives outside the Terraform configuration it serves: if the state backend were
# managed by the same configuration, `terraform destroy` would delete the state
# file describing what it is destroying, and the next `apply` would orphan every
# resource it could no longer see.
#
# The script is idempotent, so re-running it after a teardown is safe.
set -euo pipefail

LOCATION="${LOCATION:-northeurope}"
STATE_RG="${STATE_RG:-ead-tfstate-rg}"
CONTAINER="${CONTAINER:-tfstate}"

# Storage account names are globally unique, 3-24 chars, lowercase alphanumeric.
# Derive a stable name from the subscription ID so repeated runs converge on the
# same account instead of leaking a new one each time.
if [[ -z "${STATE_SA:-}" ]]; then
  sub_id=$(az account show --query id -o tsv)
  suffix=$(printf '%s' "$sub_id" | tr -d '-' | cut -c1-12)
  STATE_SA="eadtfstate${suffix}"
fi

echo "==> subscription : $(az account show --query name -o tsv)"
echo "==> resource group: ${STATE_RG}"
echo "==> storage account: ${STATE_SA}"
echo "==> container    : ${CONTAINER}"

az group create \
  --name "${STATE_RG}" \
  --location "${LOCATION}" \
  --output none

# TLS 1.2 minimum, no public blob access, and versioning so a corrupted or
# truncated state file can be recovered rather than re-imported by hand.
az storage account create \
  --name "${STATE_SA}" \
  --resource-group "${STATE_RG}" \
  --location "${LOCATION}" \
  --sku Standard_LRS \
  --kind StorageV2 \
  --min-tls-version TLS1_2 \
  --allow-blob-public-access false \
  --output none

az storage account blob-service-properties update \
  --account-name "${STATE_SA}" \
  --resource-group "${STATE_RG}" \
  --enable-versioning true \
  --output none

# --auth-mode login uses the signed-in principal rather than an account key, so
# no long-lived key is printed, exported, or stored anywhere by this script.
az storage container create \
  --name "${CONTAINER}" \
  --account-name "${STATE_SA}" \
  --auth-mode login \
  --output none

cat <<EOF

State backend ready.

Set these as GitHub Actions repository secrets so the Infrastructure workflow
can run 'terraform init':

  TFSTATE_RESOURCE_GROUP   ${STATE_RG}
  TFSTATE_STORAGE_ACCOUNT  ${STATE_SA}
  TFSTATE_CONTAINER        ${CONTAINER}

To initialise locally:

  terraform -chdir=infrastructure/terraform init \\
    -backend-config="resource_group_name=${STATE_RG}" \\
    -backend-config="storage_account_name=${STATE_SA}" \\
    -backend-config="container_name=${CONTAINER}" \\
    -backend-config="key=ead.tfstate" \\
    -backend-config="use_azuread_auth=true"

EOF
