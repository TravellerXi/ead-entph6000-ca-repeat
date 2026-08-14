# EAD CA Repeat 项目 — Enterprise Architecture Deployment

TU Dublin M.Sc. in DevOps — Enterprise Architecture Deployment (Part-Time) 模块的 CA Repeat（重修作业）工作区。

- **课程**: Enterprise Architecture Deployment (ENTP H6000), course_id=452413 (Brightspace)
- **讲师**: Dr Omar Portillo
- **作业**: CA Repeat (PT)，合并替代原 CA1(30%) + CA2(70%)，满分 100
- **截止日期**: 2026-08-23 22:59 (UTC)
- **当前状态**: 调研/规划阶段，尚未开始编码

## 目录结构

```
EAD-CA-Repeat/
├── README.md                        本文件
├── CHECKLIST.md                     按评分标准整理的可勾选任务清单
├── docs/
│   ├── 00-course-overview.md        课程13周内容通读总结（技术栈地图）
│   ├── 01-ca-repeat-requirements.md CA Repeat 完整要求 + 评分标准 + 报告大纲（文字整理版）
│   ├── 02-implementation-plan.md    架构设计 + 技术选型 + 10天执行计划
│   ├── 03-ai-collaboration-log.md   AI协作记录 + Prompt日志（供报告GenAI附录参考）
│   └── course-materials/            从 Brightspace 下载的官方原始文件
│       ├── CA-Repeat-Instructions.pdf
│       ├── CA-Repeat-MarkingScheme.pdf
│       ├── CA-Repeat-ReportOverview.pdf
│       ├── EAD_BE_CA-Repeat.zip     官方后端起始代码（原始压缩包）
│       └── EAD_FE_CA-Repeat.zip     官方前端起始代码（原始压缩包）
├── services/
│   ├── starter-backend/             已解压：官方 Spring Boot 后端起始代码（Recipe CRUD, port 8080）
│   ├── starter-frontend/            已解压：官方 Node.js 前端起始代码（port 22137）
│   ├── frontend/                    TODO: 最终前端服务（可基于 starter-frontend 扩展）
│   ├── recipe-service/              TODO: Recipe 微服务（拆分自 starter-backend）
│   ├── user-service/                TODO: 新增 User/Auth 微服务
│   └── review-service/              TODO: 新增 Review 微服务
├── infrastructure/
│   ├── helm/                        TODO: Helm charts（K8s 部署模板）
│   └── terraform/                   TODO: Terraform IaC 脚本
└── .github/workflows/               TODO: CI/CD 流水线定义
```

## 快速开始

1. 阅读 [docs/00-course-overview.md](docs/00-course-overview.md) 了解课程都教了什么技术。
2. 阅读 [docs/01-ca-repeat-requirements.md](docs/01-ca-repeat-requirements.md) 明确必须交付什么、怎么评分。
3. 阅读 [docs/02-implementation-plan.md](docs/02-implementation-plan.md) 看具体架构方案和10天计划。
4. 用 [CHECKLIST.md](CHECKLIST.md) 跟踪进度。
5. 起始代码在 `services/starter-backend` 和 `services/starter-frontend`，在此基础上拆分/扩展为4个微服务。
6. 每次请 AI 协助后，记得把 prompt 追加到 [docs/03-ai-collaboration-log.md](docs/03-ai-collaboration-log.md)，方便写报告 GenAI 附录时直接引用。

## 关键提醒

- ⚠️ Brightspace dropbox 说明要求：报告中**必须列出各 Service 的 URL 和 CI/CD pipeline 的 URL** —— 因此服务需部署到可访问的云端（AKS），纯本地 Minikube 不满足。
- ⚠️ CA Repeat 明确要求**两种不同的部署策略**（CA2 原来只要求一种）。
- ⚠️ 报告中必须**披露 GenAI 使用情况并附上所有 prompt**（TU Dublin 允许 Level 3 AI-Assisted Editing）。
- ⚠️ 录制完演示视频后，如果用了云服务（如 Azure AKS），记得**立刻销毁资源**避免产生费用。
- ⚠️ 讲师可能在9月初要求视频通话答辩，解释提交的内容。
