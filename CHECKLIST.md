# CA Repeat Delivery Checklist

> Due: **23 August 2026, 22:59 UTC**. Updated 18 August after real CI and
> Terraform-plan execution. A checked item has executable evidence, not only code.

## Architecture and Code

- [x] Four services: frontend, recipe-service, user-service, review-service
- [x] Three entities: Recipe, User, Review
- [x] Four non-root multi-stage container images
- [x] MongoDB StatefulSet, headless Service and bound PVC in the kind smoke test
- [x] Synchronous reference validation and asynchronous recipe lifecycle events
- [x] 30 tests passing (7 recipe, 7 user, 6 review, 10 frontend)

## CI, CD and Infrastructure

- [x] Matrix test/build workflow
- [x] Trivy SARIF upload and hard gate on fixed CRITICAL findings
- [x] Four public GHCR images; anonymous manifest access verified
- [x] Helm lint, blue/green render and kubeval schema validation
- [x] Terraform fmt/validate and Azure remote-state plan
- [x] End-to-end kind deployment through the frontend to MongoDB
- [x] Negative NetworkPolicy test: an unlabelled pod cannot reach a backend
- [x] RollingUpdate implementation and rollback path
- [x] Blue/green staged smoke test, promotion and Git-revert rollback path
- [x] Liveness/readiness probes, CPU HPAs and KEDA queue-depth scaling
- [x] Non-root/read-only security contexts, generated Secrets, PSA and default deny
- [x] Argo CD reconciles a dedicated `deploy` branch; `main` remains protected
- [x] Initial Terraform plan/apply: 10 add, 0 change, 0 destroy (`32161529968`)
- [x] Live AKS and Argo CD reconciliation; corrective apply was 0 add, 1 in-place change, 0 destroy (`32167126362`)
- [x] Non-destructive remote-state destroy plan: 0 add, 0 change, 10 destroy (`32174652446`)
- [x] Protected two-phase green promotion at immutable tag (`32167692249`)
- [x] Live public health/readiness, CSS and RabbitMQ cascade verified
- [ ] Successful live destroy after recording

## Additional Features (20 marks)

Screened against all 28 lecture, laboratory and demonstration documents, the 22
supplied manifests and 9 lecture transcripts (approximately 73,000 words). Of the
16 lecture recordings only 1 carried captions; 8 were transcribed locally with
whisper.cpp and the remaining 7 were covered by their same-week slides. A
technology counted as taught only where a lecture, lab walkthrough or
demonstration explained its purpose. Argo CD, KEDA, GitOps, Kafka and AMQP have
zero occurrences in any course material. RabbitMQ is never taught, but does appear
as an unexplained dependency inside the W03 AKS supplementary sample manifest
(`aks-store-quickstart.yaml`).
Prometheus/Grafana (W10), Istio (W05) and Trivy (W07) are taught by the module and
are therefore **not** claimed as additional features.

- [x] Argo CD GitOps
- [x] RabbitMQ event-driven messaging
- [x] KEDA event-driven autoscaling
- [ ] Explain all three clearly in the demonstration video

## Report

- [x] Repository and CI/CD/Infrastructure workflow URLs
- [x] Live frontend URL: http://4.245.131.37 (until post-recording destroy)
- [x] Architecture diagram (Mermaid rendered and visually verified over local HTTP)
- [x] All required research/release-plan topics
- [x] Harvard references and bibliography
- [x] Gaps, trade-offs and requirements-not-achieved disclosure
- [x] Complete 38-entry prompt log in Appendix A, checked against session store and transcript
- [ ] Student-written section 13 GenAI declaration (template supplied separately)
- [ ] Final PDF after the declaration and live URL are inserted

## Demonstration and Submission

- [x] Demonstration script timed to 18:00 and covering both required parts
- [ ] Student records the demonstration video
- [ ] Student reviews the work and confirms they can explain it
- [ ] Student submits the PDF and video to CA Repeat (PT), assignment 255278
- [ ] Destroy Azure resources immediately after recording
