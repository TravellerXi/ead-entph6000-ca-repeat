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
- [x] Terraform plan: 10 add, 0 change, 0 destroy (apply/destroy skipped in plan run)
- [ ] Successful live AKS apply and Argo CD reconciliation (in progress)
- [ ] Successful live destroy after recording

## Additional Features (20 marks)

Verified against 37 text sources and 73,383 words of lecture transcript/audio.
Prometheus/Grafana, Istio, Trivy and the deployment strategies are taught and are
therefore **not** claimed as additional features.

- [x] Argo CD GitOps
- [x] RabbitMQ event-driven messaging
- [x] KEDA event-driven autoscaling
- [ ] Explain all three clearly in the demonstration video

## Report

- [x] Repository and CI/CD/Infrastructure workflow URLs
- [ ] Live frontend URL after successful AKS deployment
- [x] Architecture diagram (Mermaid rendered and visually verified over local HTTP)
- [x] All required research/release-plan topics
- [x] Harvard references and bibliography
- [x] Gaps, trade-offs and requirements-not-achieved disclosure
- [x] Complete 35-entry prompt log in Appendix A
- [ ] Student-written section 13 GenAI declaration (template supplied separately)
- [ ] Final PDF after the declaration and live URL are inserted

## Demonstration and Submission

- [x] Demonstration script timed to 20:00 and covering both required parts
- [ ] Student records the demonstration video
- [ ] Student reviews the work and confirms they can explain it
- [ ] Student submits the PDF and video to CA Repeat (PT), assignment 255278
- [ ] Destroy Azure resources immediately after recording
