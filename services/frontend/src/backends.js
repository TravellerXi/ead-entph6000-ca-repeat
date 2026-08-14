'use strict';

const RECIPE_BASE_URL = process.env.RECIPE_SERVICE_URL || 'http://recipe-service:8080';
const USER_BASE_URL = process.env.USER_SERVICE_URL || 'http://user-service:8080';
const REVIEW_BASE_URL = process.env.REVIEW_SERVICE_URL || 'http://review-service:8080';

const DEFAULT_TIMEOUT_MS = Number(process.env.BACKEND_TIMEOUT_MS || 4000);

/** Wraps fetch with a timeout so a hung backend cannot pin a front-end worker. */
async function call(baseUrl, path, options = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), DEFAULT_TIMEOUT_MS);
  try {
    const response = await fetch(`${baseUrl}${path}`, {
      ...options,
      signal: controller.signal,
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
    });
    const text = await response.text();
    const body = text ? JSON.parse(text) : null;
    if (!response.ok) {
      const error = new Error(`${baseUrl}${path} responded ${response.status}`);
      error.status = response.status;
      error.body = body;
      throw error;
    }
    return body;
  } finally {
    clearTimeout(timer);
  }
}

module.exports = {
  RECIPE_BASE_URL,
  USER_BASE_URL,
  REVIEW_BASE_URL,

  listRecipes: () => call(RECIPE_BASE_URL, '/api/recipes'),
  createRecipe: (recipe) =>
    call(RECIPE_BASE_URL, '/api/recipes', { method: 'POST', body: JSON.stringify(recipe) }),
  deleteRecipe: (id) => call(RECIPE_BASE_URL, `/api/recipes/${id}`, { method: 'DELETE' }),
  recipeMeta: () => call(RECIPE_BASE_URL, '/api/recipes/_meta'),

  listUsers: () => call(USER_BASE_URL, '/api/users'),
  registerUser: (user) =>
    call(USER_BASE_URL, '/api/users', { method: 'POST', body: JSON.stringify(user) }),

  listReviews: (recipeId) =>
    call(REVIEW_BASE_URL, recipeId ? `/api/reviews?recipeId=${encodeURIComponent(recipeId)}` : '/api/reviews'),
  createReview: (review) =>
    call(REVIEW_BASE_URL, '/api/reviews', { method: 'POST', body: JSON.stringify(review) }),
  reviewSummary: (recipeId) =>
    call(REVIEW_BASE_URL, `/api/reviews/summary/${encodeURIComponent(recipeId)}`)
};
