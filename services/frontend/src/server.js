'use strict';

const { createApp } = require('./app');

const PORT = Number(process.env.PORT || 3000);
const app = createApp();

const server = app.listen(PORT, () => {
  console.log(JSON.stringify({ level: 'INFO', msg: 'frontend listening', port: PORT }));
});

// Let Kubernetes drain connections before the container is killed.
function shutdown(signal) {
  console.log(JSON.stringify({ level: 'INFO', msg: 'shutting down', signal }));
  server.close(() => process.exit(0));
  setTimeout(() => process.exit(1), 10000).unref();
}

process.on('SIGTERM', () => shutdown('SIGTERM'));
process.on('SIGINT', () => shutdown('SIGINT'));
