locals {
  # Deterministic names. A random suffix would make the resource group
  # unpredictable, which breaks two things on ephemeral CI runners: a later
  # `destroy` could not find what a previous `apply` created, and the CD
  # workflow could not resolve the cluster from static configuration.
  name = "${var.project}-${var.environment}"
}

resource "azurerm_resource_group" "this" {
  name     = "rg-${local.name}"
  location = var.location
  tags     = var.tags
}

resource "azurerm_log_analytics_workspace" "this" {
  name                = "log-${local.name}"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  sku                 = "PerGB2018"
  # Shortest retention Azure allows: this is a demo cluster and logs cost money.
  retention_in_days = 30
  tags              = var.tags
}

resource "azurerm_kubernetes_cluster" "this" {
  name                = "aks-${local.name}"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  dns_prefix          = local.name
  kubernetes_version  = var.kubernetes_version

  # Free tier control plane: no SLA, no cost. Appropriate for an assessed demo.
  sku_tier = "Free"

  default_node_pool {
    name                = "system"
    node_count          = var.node_count
    vm_size             = var.node_size
    os_disk_size_gb     = 32
    enable_auto_scaling = false
    upgrade_settings {
      max_surge = "33%"
    }
  }

  # Managed identity avoids storing a service principal secret anywhere.
  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_policy = "calico"
    network_plugin = "azure"
  }

  # KEDA as a managed add-on rather than a self-managed Helm release,
  # so control-plane upgrades keep it compatible.
  workload_autoscaler_profile {
    keda_enabled = true
  }

  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.this.id
  }

  role_based_access_control_enabled = true

  tags = var.tags
}
