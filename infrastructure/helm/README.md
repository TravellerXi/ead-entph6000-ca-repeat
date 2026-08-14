# Helm Charts

**TODO**：为4个服务 + MongoDB 编写 Helm chart，实现"一套 values 驱动所有环境"。

建议结构：
```
infrastructure/helm/
├── ead-app/                  一个总 umbrella chart，或每服务一个 chart
│   ├── Chart.yaml
│   ├── values.yaml           默认值（app名/端口/副本数/镜像tag等）
│   ├── values-dev.yaml       开发环境覆盖值
│   ├── values-prod.yaml      生产环境覆盖值
│   └── templates/
│       ├── frontend-deployment.yaml
│       ├── recipe-service-deployment.yaml
│       ├── user-service-deployment.yaml
│       ├── review-service-deployment.yaml
│       ├── mongodb-statefulset.yaml
│       ├── mongodb-headless-service.yaml
│       ├── *-service.yaml（各服务对应的 K8s Service）
│       └── blue-green/       Blue/Green 部署策略专用模板（参考课程 W05 demo_cmds）
```

参考课程材料：W04（Helm基础/StatefulSet/PVC）、W05（部署策略YAML）。
