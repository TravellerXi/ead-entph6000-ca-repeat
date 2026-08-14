# 课程架构总览（Week01–Week13 通读总结）

课程：Enterprise Architecture Deployment（兼职/Part-Time 班），Brightspace course_id=452413。

课程逻辑：**W01-04 搭骨架（容器 → 编排 → 包管理 → 数据层）→ W05-07 强化运维能力（发布策略 → 可观测性 → 安全）→ W10-11 高阶话题（可持续性、IaC）**。CA Repeat 本质上要求把这13周的技能点串成一个完整的企业级微服务部署项目。

| 周次 | 主题 | 核心技术/命令 |
|---|---|---|
| W01 | 容器基础 | Docker build/run/tag/push、Dockerfile 分层、Docker Hub、Minikube、kubectl 入门（`kubectl get nodes/pods`, `kubectl expose`）|
| W02 | SecDevOps + CA1 发布 | DevSecOps 案例研究（原始讲座为 Brightspace HTML 页面，未能提取纯文本）|
| W03 | KaaS（Kubernetes as a Service） | Azure AKS/ACR 全流程（`az acr create/login`, `az aks create/scale`）、K8s 对象模型 Pod → ReplicaSet → Deployment → Service、namespace/context 管理 |
| W04 | Helm & 数据层 | JAR/WAR 打包策略（Skinny/Thin/Fat/Hollow）、Helm Chart/Release/Hooks、StatefulSet、PersistentVolume/PVC、Headless Service、MongoDB/MariaDB 部署与连接 |
| W05 | 发布编排 | **四种部署策略**：Recreate / RollingUpdate / Blue-Green / Canary（均有完整 YAML 示例与 `kubectl rollout` 命令）、Ingress、`rollout history/undo`、Velero 备份恢复 |
| W06 | 监控与日志 | Liveness/Readiness 探针、PodDisruptionBudget、亲和性/反亲和性、HPA、集群自动伸缩、三种日志方案（原生 stdout/sidecar/DaemonSet）、**EFK（Elasticsearch + Fluentd + Kibana）完整部署命令** |
| W07 | 安全 | 共享责任模型、RBAC、Namespace 隔离、SecurityContext（`runAsNonRoot`、只读文件系统、Linux capabilities 最小化）、Pod Security Admission（Privileged/Baseline/Restricted）、Secrets/ConfigMap、**镜像扫描（Trivy / Clair / Docker Scout）**、Kata Containers |
| W08 | 无课（圣帕特里克节） | — |
| W09 | CA1 展示 | — |
| W10 | 可持续性 | K8s 绿色实践、HPA/VPA 资源优化、OpenCost、CAST.AI（**仅概念提及，未动手教**，适合作为"附加功能"）|
| W11 | Infrastructure as Code | **Terraform**（write → plan → apply 工作流）+ Terraform Kubernetes Provider（`providers.tf` / `k8s.tf`）|
| W12 | CA2 展示 | — |
| W13 | 复习周 | — |

## 前置知识假设

课程简介（Week01 Intro 幻灯片）明确提到本模块建立在前置模块 **Continuous Software Delivery (CSFD)** 之上，那门课已经教过 Azure App Service 单体应用的 CI/CD 流水线。因此 EAD 模块本身**没有重新教 CI 工具**（如 GitHub Actions/Azure Pipelines 的具体用法），默认学生已掌握，重点在于 CD（持续部署）、发布编排与容器编排层面。这意味着做 CA Repeat 时，CI 流水线工具可以自由选择（推荐 GitHub Actions，免费且对个人项目友好）。

## 官方起始代码（已下载并解压到 `services/starter-*`）

- **starter-backend**：Spring Boot（Maven）应用，`mvnw spring-boot:run` 启动，默认端口 8080。已实现4个 REST 端点：
  - `GET /` 健康检查
  - `GET /recipes` 获取所有食谱
  - `POST /recipe` 新增食谱
  - `DELETE /recipe/{name}` 删除食谱
- **starter-frontend**：Node.js 应用，`npm install && node fe-server.js` 启动，默认端口 22137，通过 `config/config.json` 配置后端地址。

这套起始代码只有 **2个服务**（FE + BE）+ MongoDB，CA Repeat 要求至少 **4个服务、涉及2个不同实体**，因此必须在此基础上拆分/扩展（详见 [02-implementation-plan.md](02-implementation-plan.md)）。
