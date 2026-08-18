# GitHub Actions

The repository implements three workflows:

| File | Responsibility |
|---|---|
| `ci.yml` | Tests all four services; lints and schema-checks Helm; validates Terraform; builds, scans and publishes four images; deploys the exact commit to kind and exercises a frontend-to-MongoDB round trip |
| `cd.yml` | Writes staged and promoted desired state to the `deploy` branch; Argo CD is the only deployment authority; smoke-tests the inactive blue/green slot before switching traffic |
| `infrastructure.yml` | Runs read-only plan/plan-destroy and protected apply/destroy against remote Azure state |

Verified CI evidence: run `32167076883` completed all ten jobs successfully,
including 30 tests, Trivy gates, PVC binding, HTTP persistence, RabbitMQ
cascade and a negative NetworkPolicy test. Read-only Infrastructure run
`32174652446` resolved the live state as 0 add, 0 change and 10 destroy.
