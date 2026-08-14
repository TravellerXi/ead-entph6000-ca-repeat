# user-service

**TODO**：新增微服务（课程起始代码中不存在，需要从零搭建，语言/框架建议与 recipe-service 保持一致以复用CI模板，例如同为 Spring Boot）。

- 拥有实体：**User**
- 建议端点：`POST /register`、`POST /login`、`GET /users/{id}`
- 数据层：MongoDB 独立 database/collection（如 `userdb.users`），密码需加密存储（不要明文，安全评分项会关注）
- 认证：可选 JWT，供 frontend 和 review-service 校验请求身份
