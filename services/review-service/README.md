# review-service

Spring Boot service that owns `Review` and `reviewdb`. A review references both
a recipe and an author; synchronous validation fails closed with HTTP 422 when
either peer is unavailable or reports a missing entity. A RabbitMQ listener
consumes recipe lifecycle events and removes orphaned reviews.

Endpoints live under `/api/reviews`, including `summary/{recipeId}`.

```bash
mvn test
```

Six tests cover validation, CRUD, summary and failure behaviour.
