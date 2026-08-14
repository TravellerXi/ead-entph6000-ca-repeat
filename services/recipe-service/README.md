# recipe-service

**TODO**：从 `../starter-backend`（官方 Spring Boot 起始代码）拆分出来的 Recipe 微服务。

- 参考起始代码：[../starter-backend](../starter-backend)（Maven, `mvnw spring-boot:run`, 默认 port 8080）
- 拥有实体：**Recipe**
- 沿用原有4个端点：`GET /`、`GET /recipes`、`POST /recipe`、`DELETE /recipe/{name}`
- 数据层：MongoDB 独立 database/collection（如 `recipedb.recipes`）
- 需要暴露给 review-service 的接口：按 recipe 名称/ID 查询是否存在（供 review-service 校验引用）
