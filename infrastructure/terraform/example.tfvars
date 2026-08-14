# Copy to terraform.tfvars and fill in. terraform.tfvars is gitignored.
subscription_id = "d720f7bc-7218-4e03-b741-dfe843444823"
location        = "northeurope"
node_count      = 2
node_size       = "Standard_B2s"
enable_argocd   = true
gitops_repo_url = "https://github.com/TravellerXi/ead-ca-repeat.git"
