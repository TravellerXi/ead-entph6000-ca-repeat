# frontend

Server-rendered Node.js/Express UI and the platform's only public workload.
Backend URLs are supplied through environment variables. `/healthz` is local
liveness; `/readyz` checks recipe-service without making liveness depend on a
downstream system.

```bash
npm ci
npm test
npm start
```

The 10 tests include HTML escaping and the liveness/readiness separation.
