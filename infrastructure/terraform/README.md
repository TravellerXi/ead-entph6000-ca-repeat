# Terraform Infrastructure

Terraform creates the Azure resource group, Log Analytics workspace, AKS
cluster, platform namespace and credentials, then installs Argo CD and its
Application. Argo CD reconciles the application chart from the `deploy` branch.

State is stored in a separate Azure Storage account so a later ephemeral runner
can destroy resources created by an earlier one. Bootstrap it once:

```bash
bash scripts/bootstrap-state.sh
```

The Infrastructure workflow exposes read-only `plan`/`plan-destroy` plus
protected `apply`/`destroy` actions. Apply run `32161529968` created the
platform. Read-only run `32174652446` then resolved the real remote state as
**0 to add, 0 to change, 10 to destroy**; its apply/destroy steps were skipped.
