# Demonstration Recording Script

**Constraint:** ≤20:00 total (Brightspace dropbox requirement).
**Two mandated parts:** (a) pipeline and project setup, explicitly naming the additional features; (b) a presentation of the Release Management Plan highlights.

> Record with screen capture plus microphone. Speak to *why*, not only *what* — the lecturer may request a follow-up call and marks are deducted if the work cannot be explained.

---

## Pre-flight checklist (do before pressing record)

- [ ] `terraform apply` completed; cluster running
- [ ] `kubectl get pods -n ead-platform` all `Running`
- [ ] Frontend LoadBalancer IP resolved and page loads
- [ ] Argo CD UI reachable and logged in
- [ ] A green-slot image tag built and ready to promote
- [ ] Browser tabs pre-opened: GitHub Actions, GHCR packages, code scanning alerts, Argo CD, frontend
- [ ] Terminal font enlarged; notifications silenced

---

## Part A — Pipeline and project setup (11:30)

### A1 · Architecture orientation — 1:30
Show `README.md` architecture diagram.

> "Four services and a MongoDB data layer. Three entities: Recipe, User, Review. Review holds foreign references to the other two, so inter-service communication is real rather than decorative. Two communication styles: synchronous REST where the caller needs an immediate answer, asynchronous messaging where it does not."

Show `services/` tree, then `ReviewController.create()` — point at the two `referenceValidator` calls and the 422.

### A2 · Repository and pipeline structure — 1:30
Open `.github/workflows/ci.yml`.

> "One repository, one job per service via a build matrix. The brief asks whether each service should have its own repository and pipeline — I chose monorepo because at this size the coordination cost of four synchronised workflow files dominates the benefit, and the matrix preserves independent test, scan and publish per service."

Walk the five stages: tests → static validation → build+scan → publish → ephemeral cluster.

### A3 · CI run, live — 2:00
Push a trivial commit, or open the latest run.

Show in order:
1. Matrix jobs green — **"30 unit tests"**
2. Helm lint + the blue/green render assertion
3. `terraform fmt -check` and `validate`
4. **Trivy scan** — then open the Security tab to show the SARIF results
5. GHCR packages, tagged by commit SHA
6. The `kind` smoke-test job — **"this proves the API server accepts the manifests, which rendering alone does not"**

### A4 · Infrastructure as Code — 2:00
Open `infrastructure/terraform/main.tf`.

> "The module taught Terraform with the Kubernetes provider, creating objects inside an existing cluster, and created AKS separately with `az aks create`. Here the `azurerm` provider creates the cluster itself, so one `terraform apply` goes from an empty subscription to a running platform."

Point at: `sku_tier = "Free"`, `identity { SystemAssigned }`, `workload_autoscaler_profile { keda_enabled }`, PSA labels in `kubernetes.tf`, `random_password` secrets.

Show `infrastructure.yml` with its `plan`/`apply`/`destroy` choice behind a protected environment.

### A5 · Deployment strategy 1 — RollingUpdate + rollback — 1:30
```
kubectl -n ead-platform get deploy recipe-service -o jsonpath='{.spec.strategy}'
kubectl -n ead-platform rollout history deployment/recipe-service
./scripts/rollback.sh rolling recipe-service
```

> "maxUnavailable is zero, so capacity never dips during a release. Rollback is ReplicaSet history — about thirty seconds."

### A6 · Deployment strategy 2 — Blue/Green + instant rollback — 2:00
Show the frontend badge (currently **blue**).

```
kubectl -n ead-platform get svc review-service -o jsonpath='{...active-colour}'
./scripts/switch-colour.sh green
```

Refresh the page — badge turns **green**.

> "Both slots run side by side. The release is a Service selector change, not a pod rollout, so cut-over and rollback are near-instant. The script smoke-tests the idle slot through its colour-pinned Service before any traffic moves."

Roll back: `./scripts/rollback.sh bluegreen` — badge returns to blue.

### A7 · Additional features — 3:00 ⚠ **highest-value segment, 20 marks**
State plainly on camera:

> "Three technologies not taught in this module: Argo CD, RabbitMQ and KEDA. I verified this by reading every lecture and lab in Weeks 1 to 11 — including the Week 2 guest material — before choosing. I originally planned Prometheus and Grafana and rejected them, because Week 10 covers both and they would have scored zero."

**Argo CD** — open the UI, show the `ead-platform` Application `Synced`/`Healthy`. Then:
```
kubectl -n ead-platform scale deployment/frontend --replicas=5
```
Watch Argo CD revert it.
> "Self-heal. Drift is corrected without a pipeline run."

**RabbitMQ** — open the management UI. Create a recipe in the frontend, show `recipe.created` on the exchange. Delete it, show reviews cascade away.
> "recipe-service does not know review-service exists."

**KEDA** — show the `ScaledObject`, then:
```
kubectl -n ead-platform get scaledobject,hpa
```
> "The module taught HPA on CPU. CPU is a poor signal for a consumer blocked on I/O — queue depth is the signal that actually indicates pending work."

---

## Part B — Release Management Plan highlights (7:00)

Slides, not a document read-through.

### B1 · Orchestration and delivery choices — 1:30
AKS on Free tier; GitHub Actions; **continuous delivery, not deployment** — approval gate makes promotion auditable.

### B2 · The release plan itself — 2:00
Change process end to end: branch → PR → CI → review → merge → publish → approval → deploy → Argo CD holds state.

Three rollback tiers table. Emphasise blue/green is fastest *because the previous version never stopped running*.

### B3 · Backup and recovery — 1:00
```
./scripts/backup-mongodb.sh
./scripts/restore-mongodb.sh backups/mongodb-<stamp>.archive.gz
```
> "`mongodump` runs inside the pod, so no database port is exposed. Restore uses `--drop`, so replaying the same archive twice yields the same state — that idempotence is what makes recovery drills safe to rehearse."

### B4 · Security — 1:30
Four layers: cloud (managed identity, RBAC, PSA) · pipeline (least-privilege tokens, Trivy gate) · container (non-root, read-only rootfs, dropped capabilities) · data (Secrets, BCrypt, NetworkPolicy default-deny).

Then the starter-code findings:
> "Three real defects in the supplied project: credentials hard-coded twice, and `actuator` exposing `env`, which serves the entire configuration — including those credentials — over HTTP. I found these by reading the code, and fixed all three."

### B5 · Sustainability and honest gaps — 1:00
Ten pods on two nodes; requests and limits everywhere; KEDA scales the consumer down when idle; `terraform destroy` makes the footprint zero between demonstrations.

State the gaps plainly: no metrics backend or alerting; no scheduled off-site backup; no load testing; single-replica MongoDB and RabbitMQ; local Terraform state.

---

## Close — 0:30
> "Every requirement in the brief is addressed. The gaps I have named are deliberate trade-offs against a two-node cluster and a ten-day window, and each is documented in section 14 of the report."

Show the URLs on screen (frontend, Argo CD, CI, CD).

---

## Timing

| Segment | Duration | Cumulative |
|---|---:|---:|
| A1 Architecture | 1:30 | 1:30 |
| A2 Pipeline structure | 1:30 | 3:00 |
| A3 CI run | 2:00 | 5:00 |
| A4 IaC | 1:30 | 6:30 |
| A5 RollingUpdate | 1:30 | 8:00 |
| A6 Blue/Green | 2:00 | 10:00 |
| A7 Additional features | 3:00 | 13:00 |
| B1 Choices | 1:30 | 14:30 |
| B2 Release plan | 2:00 | 16:30 |
| B3 Backup | 1:00 | 17:30 |
| B4 Security | 1:00 | 18:30 |
| B5 Sustainability | 1:00 | 19:30 |
| Close | 0:30 | **20:00** |

> **Budget met exactly at 20:00**, against a hard 20:00 limit. There is no
> slack, so if a live step stalls, cut B5 first and A1 second. Never cut A7:
> the additional-features segment carries 20 marks, more than any other
> segment, and half of those marks depend on explaining the features aloud.

---

## Post-recording

- [ ] Verify total ≤20:00
- [ ] Confirm all three additional features are named aloud
- [ ] Confirm both deployment strategies demonstrated live
- [ ] Copy the on-screen URLs into report §11
- [ ] **`terraform destroy`** — tear down billable resources
- [ ] Upload PDF + video to the CA Repeat (PT) dropbox
