# CI/CD 流水线（GitHub Actions）

**TODO**：为4个服务编写 GitHub Actions workflow，实现真正自动化的 CI/CD（对应评分标准 CI 10分 + CD 4分）。

建议流水线阶段：
1. **Lint/Test**：各服务的单元测试
2. **Build**：为每个服务构建 Docker 镜像（可用 matrix 策略并行构建4个服务）
3. **Scan**：镜像安全扫描（Trivy 或 Docker Scout），作为安全 4 分的直接证据
4. **Push**：推送镜像到 Docker Hub 或 Azure Container Registry
5. **Deploy**：`helm upgrade` 或 `terraform apply` 自动部署到目标集群
   - 如用 Minikube：需要自托管 runner（在本机跑 `minikube start` + workflow runner）
   - 如用 AKS：GitHub 托管 runner 可直接 `az aks get-credentials` 后部署
6.（可选，附加功能）改为 **ArgoCD** GitOps 拉取式部署，替代 push 式的 Deploy 阶段

建议文件命名：`.github/workflows/ci-cd.yml`（或按服务拆分为 `frontend.yml`、`recipe-service.yml` 等，报告里需要讨论"每个服务是否有独立CI/CD流水线"这一决策）。
