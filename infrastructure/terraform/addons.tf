# Additional feature: Argo CD provides GitOps-driven continuous deployment.
#
# Deployment authority is Argo CD, not the pipeline. The CD workflow's job ends
# at committing a desired-state file to Git; Argo CD reconciles the cluster
# toward it. Running `helm upgrade` from the pipeline as well would give two
# controllers write access to the same objects, and Argo's selfHeal would revert
# whatever the pipeline did.
resource "helm_release" "argocd" {
  count = var.enable_argocd ? 1 : 0

  name             = "argocd"
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = "7.6.12"
  namespace        = "argocd"
  create_namespace = true
  timeout          = 900

  values = [yamlencode({
    global = {
      # Keep Argo CD small: this assessment cluster has only two nodes.
      resources = {
        requests = { cpu = "50m", memory = "128Mi" }
      }
    }
    server = {
      # ClusterIP, not LoadBalancer. The report claims the frontend is the only
      # externally reachable workload; a public Argo CD endpoint would make that
      # false and would expose a deployment control plane to the internet.
      # Reach the UI with:
      #   kubectl -n argocd port-forward svc/argocd-server 8080:443
      service   = { type = "ClusterIP" }
      extraArgs = ["--insecure"]
    }
    dex            = { enabled = false }
    notifications  = { enabled = false }
    applicationSet = { enabled = false }
  })]

  depends_on = [azurerm_kubernetes_cluster.this]
}

# The Application is created through the argocd-apps chart rather than a
# kubernetes_manifest resource. kubernetes_manifest performs schema discovery at
# plan time, so it fails when the CRD it needs is installed by another resource
# in the same run; depends_on cannot fix a plan-time lookup.
resource "helm_release" "argocd_apps" {
  count = var.enable_argocd && var.gitops_repo_url != "" ? 1 : 0

  name       = "argocd-apps"
  repository = "https://argoproj.github.io/argo-helm"
  chart      = "argocd-apps"
  version    = "2.0.2"
  namespace  = "argocd"
  timeout    = 600

  values = [yamlencode({
    applications = {
      "ead-platform" = {
        namespace = "argocd"
        project   = "default"
        source = {
          repoURL        = var.gitops_repo_url
          targetRevision = var.gitops_revision
          path           = "infrastructure/helm/ead-platform"
          helm = {
            # values-live.yaml carries the image tag and active colour written
            # by the CD workflow. Ordering matters: later files win.
            valueFiles              = ["values.yaml", "values-live.yaml"]
            ignoreMissingValueFiles = true
          }
        }
        destination = {
          server    = "https://kubernetes.default.svc"
          namespace = var.namespace
        }
        syncPolicy = {
          automated = {
            prune    = true
            selfHeal = true
          }
          syncOptions = ["CreateNamespace=false"]
        }
      }
    }
  })]

  depends_on = [helm_release.argocd, kubernetes_namespace.platform]
}
