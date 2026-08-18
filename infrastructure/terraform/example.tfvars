# Copy to terraform.tfvars and fill in. terraform.tfvars is gitignored.
subscription_id = "00000000-0000-0000-0000-000000000000"
location        = "northeurope"
node_count      = 2
node_size       = "Standard_EC2as_v5"
enable_argocd   = true
gitops_repo_url = "https://github.com/TravellerXi/ead-entph6000-ca-repeat.git"
gitops_revision = "deploy"
