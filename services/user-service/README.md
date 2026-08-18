# user-service

Spring Boot service that owns `User` and `userdb`. Registration hashes passwords
with BCrypt; password material is write-only on input and never returned. Login
uses a uniform 401 response to avoid username enumeration.

Endpoints live under `/api/users`, including `/login` and `/{id}/exists`.

```bash
mvn test
```

Seven tests cover registration, login, duplicate users, password exposure and
existence checks.
