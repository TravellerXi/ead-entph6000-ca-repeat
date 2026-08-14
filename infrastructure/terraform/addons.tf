# Additional feature: Argo CD provides GitOps-driven continuous deployment.
# The pipeline stops at "push a manifest change"; Argo CD owns convergence,
# which is what makes the cluster state self-healing rather than pipeline-driven.
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
      # Keep Argo CD small: this cluster has 2 x B2s nodes.
      resources = {
        requests = { cpu = "50m", memory = "128Mi" }
      }
    }
    server = {
      service = {
        type = "LoadBalancer"
      }
      extraArgs = ["--insecure"]
    }
    dex            = { enabled = false }
    notifications  = { enabled = false }
    applicationSet = { enabled = false }
  })]

  depends_on = [azurerm_kubernetes_cluster.this]
}

# Registers the platform chart as an Argo CD Application so that any commit to
# the repository is reconciled automatically, including drift correction.
resource "kubernetes_manifest" "argocd_application" {
  count = var.enable_argocd && var.gitops_repo_url != "" ? 1 : 0

  manifest = {
    apiVersion = "argoproj.io/v1alpha1"
    kind       = "Application"
    metadata = {
      name      = "ead-platform"
      namespace = "argocd"
    }
    spec = {
      project = "default"
      source = {
        repoURL        = var.gitops_repo_url
        targetRevision = "main"
        path           = "infrastructure/helm/ead-platform"
        helm = {
          valueFiles = ["values.yaml"]
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

  depends_on = [helm_release.argocd, kubernetes_namespace.platform]
}
