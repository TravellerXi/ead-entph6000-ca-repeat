# Helm Chart

`ead-platform/` renders the four application services, MongoDB, RabbitMQ,
NetworkPolicies, HPAs and a KEDA `ScaledObject` from one values hierarchy.

- RollingUpdate (`maxUnavailable: 0`) protects recipe, user and frontend.
- Two review-service Deployments and colour-pinned Services implement blue/green.
- MongoDB and RabbitMQ use StatefulSets and PVCs.
- Every workload runs non-root with a read-only root filesystem.
- Default-deny ingress is opened only for the required service paths.

Validation:

```bash
helm lint infrastructure/helm/ead-platform
helm template ead infrastructure/helm/ead-platform
```
