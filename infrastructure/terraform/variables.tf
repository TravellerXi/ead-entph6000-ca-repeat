variable "subscription_id" {
  description = "Azure subscription that will host the platform."
  type        = string
}

variable "project" {
  description = "Short name used as a prefix for every Azure resource."
  type        = string
  default     = "eadrepeat"

  validation {
    condition     = can(regex("^[a-z0-9]{3,12}$", var.project))
    error_message = "project must be 3-12 lowercase alphanumeric characters."
  }
}

variable "location" {
  description = "Azure region. North Europe keeps data in Ireland, matching the course context."
  type        = string
  default     = "northeurope"
}

variable "environment" {
  description = "Environment discriminator used in resource names. Keeps names deterministic so destroy and CD can resolve them."
  type        = string
  default     = "prod"
}

variable "kubernetes_version" {
  description = "AKS control plane version. Null tracks the region default."
  type        = string
  default     = null
}

variable "node_count" {
  description = "Number of nodes in the default pool."
  type        = number
  default     = 2

  validation {
    condition     = var.node_count >= 2 && var.node_count <= 5
    error_message = "node_count must stay between 2 and 5 to remain inside the subscription vCPU quota."
  }
}

variable "node_size" {
  description = "VM size for the default pool. B2s is the cheapest size that satisfies AKS system requirements."
  type        = string
  default     = "Standard_B2s"
}

variable "namespace" {
  description = "Kubernetes namespace for the platform workloads."
  type        = string
  default     = "ead-platform"
}

variable "enable_argocd" {
  description = "Install Argo CD for GitOps-driven delivery (additional feature)."
  type        = bool
  default     = true
}

variable "gitops_repo_url" {
  description = "Repository Argo CD reconciles against."
  type        = string
  default     = ""
}

variable "gitops_revision" {
  description = "Branch or tag Argo CD tracks."
  type        = string
  default     = "main"
}

variable "tags" {
  description = "Tags applied to every Azure resource, used for cost attribution and clean-up."
  type        = map(string)
  default = {
    project    = "ead-ca-repeat"
    course     = "ENTP-H6000"
    managed-by = "terraform"
    lifecycle  = "ephemeral"
  }
}
