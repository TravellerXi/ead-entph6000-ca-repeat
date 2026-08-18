# recipe-service

Spring Boot service that owns `Recipe` and the `recipedb` database. CRUD is
exposed under `/api/recipes`; `/{id}/exists` supports reference validation.
Create and delete operations publish lifecycle events to RabbitMQ without making
the REST request fail when the broker is temporarily unavailable.

```bash
mvn test
mvn spring-boot:run
```

Six controller tests cover CRUD and metadata behaviour.
