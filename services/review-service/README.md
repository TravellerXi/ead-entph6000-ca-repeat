# review-service

**TODO**：新增微服务（课程起始代码中不存在），负责食谱评价/评分功能。

- 拥有实体：**Review**（关联 recipe_id、user_id，天然演示"服务间通信"）
- 建议端点：`POST /review`（新增评价）、`GET /reviews/recipe/{recipeId}`、`GET /reviews/user/{userId}`
- 数据层：MongoDB 独立 database/collection（如 `reviewdb.reviews`）
- 服务间通信：调用 recipe-service 校验 recipe 是否存在、调用 user-service 校验 user 是否存在
  - 若采用消息队列（附加功能之一），可改为异步发布"评价已创建"事件，由 recipe-service 订阅更新平均评分
