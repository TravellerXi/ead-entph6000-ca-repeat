# GitHub Actions

The repository implements three workflows:

| File | Responsibility |
|---|---|
| `ci.yml` | Tests all four services; lints and schema-checks Helm; validates Terraform; builds, scans and publishes four images; deploys the exact commit to kind and exercises a frontend-to-MongoDB round trip |
| `cd.yml` | Writes staged and promoted desired state to the `deploy` branch; Argo CD is the only deployment authority; smoke-tests the inactive blue/green slot before switching traffic |
| `infrastructure.yml` | Runs Terraform plan/apply/destroy against remote Azure state behind protected GitHub environments |

Verified CI evidence: run `31992514705` completed all ten jobs successfully,
including Trivy, PVC binding, the application round trip and a negative
NetworkPolicy test.
