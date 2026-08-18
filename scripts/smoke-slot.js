const service = process.argv[2];
if (!service || !/^review-service-(blue|green)$/.test(service)) {
  console.error('usage: node smoke-slot.js review-service-<blue|green>');
  process.exit(2);
}

(async () => {
  for (const path of ['/actuator/health/readiness', '/api/reviews/_meta']) {
    const response = await fetch(`http://${service}:8080${path}`, {
      signal: AbortSignal.timeout(10_000),
    });
    const body = await response.text();
    if (!response.ok) throw new Error(`${path}: HTTP ${response.status} ${body.slice(0, 160)}`);
    console.log(`${service}${path}: ${body}`);
  }
})().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
