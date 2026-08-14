output "resource_group_name" {
  description = "Resource group holding every provisioned object; deleting it removes all cost."
  value       = azurerm_resource_group.this.name
}

output "cluster_name" {
  description = "AKS cluster name."
  value       = azurerm_kubernetes_cluster.this.name
}

output "namespace" {
  description = "Namespace the platform is deployed into."
  value       = kubernetes_namespace.platform.metadata[0].name
}

output "get_credentials_command" {
  description = "Command that wires kubectl to the new cluster."
  value       = "az aks get-credentials --resource-group ${azurerm_resource_group.this.name} --name ${azurerm_kubernetes_cluster.this.name} --overwrite-existing"
}

output "frontend_url_command" {
  description = "Resolves the public URL to quote in the Release Management Plan."
  value       = "kubectl -n ${var.namespace} get svc frontend -o jsonpath='{.status.loadBalancer.ingress[0].ip}'"
}

output "argocd_access_command" {
  description = "Argo CD is ClusterIP-only by design; reach the UI through a port-forward."
  value       = var.enable_argocd ? "kubectl -n argocd port-forward svc/argocd-server 8080:443" : "argocd disabled"
}

output "argocd_initial_password_command" {
  description = "Retrieves the bootstrap admin password."
  value       = var.enable_argocd ? "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d" : "argocd disabled"
}

output "kube_config" {
  description = "Raw kubeconfig for the cluster."
  value       = azurerm_kubernetes_cluster.this.kube_config_raw
  sensitive   = true
}
