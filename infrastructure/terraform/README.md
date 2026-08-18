# Terraform Infrastructure

Terraform creates the Azure resource group, Log Analytics workspace, AKS
cluster, platform namespace and credentials, then installs Argo CD and its
Application. Argo CD reconciles the application chart from the `deploy` branch.

State is stored in a separate Azure Storage account so a later ephemeral runner
can destroy resources created by an earlier one. Bootstrap it once:

```bash
bash scripts/bootstrap-state.sh
```

The Infrastructure workflow exposes audited `plan`, `apply` and `destroy`
actions. Verified plan run `32131375061` reported **10 to add, 0 to change, 0
to destroy**; apply and destroy were skipped.
