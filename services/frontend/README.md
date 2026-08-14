# frontend 服务

**TODO**：最终前端服务。可基于 `../starter-frontend`（官方起始代码，Node.js，port 22137）扩展，新增调用 `user-service` 和 `review-service` 的页面/接口，而不仅是 `recipe-service`。

- 参考起始代码：[../starter-frontend](../starter-frontend)
- 需要新增的调用：
  - 用户登录/注册 → user-service
  - 查看/发表食谱评价 → review-service
  - 原有的食谱增删查 → recipe-service（沿用原逻辑，改指向新的服务地址）
- 配置：通过 `config/config.json` 管理三个后端服务的地址，避免硬编码。
