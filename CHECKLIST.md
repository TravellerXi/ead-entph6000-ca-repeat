# CA Repeat 任务清单

> 截止日期：**2026-08-23 22:59 (UTC)**。今天：2026-08-13。详细方案见 [docs/02-implementation-plan.md](docs/02-implementation-plan.md)。

## 架构 & 代码（对应 40分 CI/CD + 4分持久层）

- [ ] 拆分/新建 4 个服务：frontend、recipe-service、user-service、review-service
- [ ] 确认涉及 ≥2 个不同实体（Recipe / User / Review）
- [ ] 每个服务编写 Dockerfile 并本地构建通过
- [ ] MongoDB StatefulSet + PVC + Headless Service 部署（持久层）
- [ ] 服务间通信打通（review-service 引用 recipe_id / user_id）

## CI/CD 流水线（40分）

- [ ] CI：GitHub Actions 完成 build + test（10分）
- [ ] 镜像安全扫描接入流水线（Trivy 或 Docker Scout）
- [ ] CD：自动部署到 Minikube/AKS（Helm 或 kubectl apply）
- [ ] 部署策略 1：RollingUpdate 演示 + 回滚（`kubectl rollout undo`）
- [ ] 部署策略 2：Blue/Green 演示 + 流量切换
- [ ] 监控：Liveness/Readiness 探针 + HPA
- [ ] 安全：SecurityContext（非root/只读文件系统）+ Secrets + Namespace隔离
- [ ] Infrastructure as Code：Terraform 脚本可 `apply`/`destroy` 完整重建（10分，重点投入）

## 附加功能（20分，至少3个模块未教过的技术）

- [ ] 附加功能1：______________________（建议 ArgoCD）
- [ ] 附加功能2：______________________（建议 Prometheus + Grafana）
- [ ] 附加功能3：______________________（建议 RabbitMQ/Kafka）

## 报告（30分，~2000字）

- [ ] **报告中列出所有 Service 的可访问 URL**（Brightspace dropbox 硬性要求）
- [ ] **报告中列出 CI/CD Pipeline 的 URL**（GitHub Actions 运行页面）
- [ ] 按官方 ReportOverview 要求的格式排版（可读性也计分）
- [ ] 微服务应用总览（架构图、数据层设计）
- [ ] 容器编排API选型与评估
- [ ] 持续软件交付（CI/CD工具、仓库/流水线策略）
- [ ] 部署策略详述 + 回滚 + 备份恢复 + 伸缩策略
- [ ] 自动化基础设施交付
- [ ] 持续配置管理计划
- [ ] 监控与日志策略
- [ ] 安全（云/流水线/容器/数据四个维度）
- [ ] 可持续性（优势/弱点/权衡）
- [ ] GenAI 使用说明 + prompt 附录
- [ ] 引言 + 结论 + 差距/缺陷说明
- [ ] Harvard 格式参考文献
- [ ] 说明未达成的要求 + 已实现的附加功能

## 演示与提交（10分）

- [ ] 录制演示视频（≤20分钟），必须包含两部分：
  - [ ] (a) pipeline 与 project setup 演示，并明确指出新增的附加功能
  - [ ] (b) RMP 报告要点的 presentation
- [ ] 上传 PDF 报告 + 视频到 Brightspace CA Repeat (PT) dropbox（assignment_id=255278）
- [ ] 提交后如使用云资源，立刻销毁（避免产生费用）
- [ ] 提前1天提交，预留缓冲时间
- [ ] 留意9月初可能的讲师视频通话答辩邮件
