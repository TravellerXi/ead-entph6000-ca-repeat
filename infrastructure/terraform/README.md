# Terraform IaC

**TODO**：声明式 provision 所有 K8s 资源（namespace/deployment/service），实现"完全自动化的 provisioning + 可靠销毁重建"。

建议文件：
```
infrastructure/terraform/
├── providers.tf     声明 kubernetes provider（连接 Minikube 或 AKS）
├── namespace.tf      dev/prod namespace
├── mongodb.tf        MongoDB StatefulSet + PVC + Service
├── services.tf       4个服务的 Deployment + Service（或直接 helm_release 资源调用 ../helm 里的chart）
├── variables.tf
└── outputs.tf
```

工作流（对应课程 W11）：
```
terraform init
terraform plan
terraform apply
# 验证...
terraform destroy   # 必须能完整销毁重建，报告里要专门讨论这一点
```

进阶加分项：用 `azurerm` provider 直接 provision AKS 集群本身（而不只是集群内对象），并录制"从0到完整环境"的一次性演示，然后销毁。
