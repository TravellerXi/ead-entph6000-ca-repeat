const sleep = (milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds));

async function request(url, options = {}, expected = [200]) {
  const response = await fetch(url, options);
  const text = await response.text();
  if (!expected.includes(response.status)) {
    throw new Error(`${options.method || 'GET'} ${url}: HTTP ${response.status} ${text.slice(0, 200)}`);
  }
  return text ? JSON.parse(text) : null;
}

const json = (body) => ({
  method: 'POST',
  headers: { 'content-type': 'application/json' },
  body: JSON.stringify(body),
});

(async () => {
  const suffix = Date.now().toString(36);
  let recipe;
  let user;
  let review;

  try {
    recipe = await request('http://recipe-service:8080/api/recipes', json({
      name: `event-check-${suffix}`,
      ingredients: ['integration'],
      prepTimeInMinutes: 2,
    }), [201]);

    user = await request('http://user-service:8080/api/users', json({
      username: `event-${suffix}`,
      email: `event-${suffix}@example.test`,
      password: 'Temporary-12345',
      displayName: 'Integration Check',
    }), [201]);

    review = await request('http://review-service:8080/api/reviews', json({
      recipeId: recipe.id,
      authorId: user.id,
      rating: 5,
      comment: 'integration check',
    }), [201]);
    console.log('synchronous references: review created');

    await request(`http://recipe-service:8080/api/recipes/${recipe.id}`, { method: 'DELETE' }, [204]);
    recipe = null;

    let cascaded = false;
    for (let attempt = 0; attempt < 20; attempt += 1) {
      const response = await fetch(`http://review-service:8080/api/reviews/${review.id}`);
      if (response.status === 404) {
        cascaded = true;
        break;
      }
      await sleep(1000);
    }
    if (!cascaded) throw new Error('review was not removed after recipe.deleted');

    review = null;
    console.log('RabbitMQ cascade: review removed after recipe.deleted');
  } finally {
    if (review) await fetch(`http://review-service:8080/api/reviews/${review.id}`, { method: 'DELETE' });
    if (recipe) await fetch(`http://recipe-service:8080/api/recipes/${recipe.id}`, { method: 'DELETE' });
    if (user) await fetch(`http://user-service:8080/api/users/${user.id}`, { method: 'DELETE' });
  }
})().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
