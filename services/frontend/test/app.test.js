'use strict';

jest.mock('../src/backends');

const request = require('supertest');
const backends = require('../src/backends');
const { createApp, esc } = require('../src/app');

const app = createApp();

beforeEach(() => jest.resetAllMocks());

describe('probes', () => {
  test('liveness does not depend on backends', async () => {
    const res = await request(app).get('/healthz');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
    expect(backends.recipeMeta).not.toHaveBeenCalled();
  });

  test('readiness reports 503 when recipe-service is down', async () => {
    backends.recipeMeta.mockRejectedValue(new Error('connect ECONNREFUSED'));
    const res = await request(app).get('/readyz');
    expect(res.status).toBe(503);
    expect(res.body.status).toBe('NOT_READY');
  });

  test('readiness reports 200 when recipe-service answers', async () => {
    backends.recipeMeta.mockResolvedValue({ service: 'recipe-service' });
    const res = await request(app).get('/readyz');
    expect(res.status).toBe(200);
  });
});

describe('recipe listing', () => {
  test('renders recipes with review summary', async () => {
    backends.listRecipes.mockResolvedValue([
      { id: 'r1', name: 'elotes', ingredients: ['corn', 'lime'], prepTimeInMinutes: 35 }
    ]);
    backends.reviewSummary.mockResolvedValue({ reviewCount: 2, averageRating: 4.5 });

    const res = await request(app).get('/');
    expect(res.status).toBe(200);
    expect(res.text).toContain('elotes');
    expect(res.text).toContain('corn, lime');
    expect(res.text).toContain('4.5');
  });

  test('still renders recipes when review-service fails', async () => {
    backends.listRecipes.mockResolvedValue([
      { id: 'r1', name: 'elotes', ingredients: ['corn'], prepTimeInMinutes: 35 }
    ]);
    backends.reviewSummary.mockRejectedValue(new Error('review-service down'));

    const res = await request(app).get('/');
    expect(res.status).toBe(200);
    expect(res.text).toContain('elotes');
  });

  test('returns 502 when recipe-service is unavailable', async () => {
    backends.listRecipes.mockRejectedValue(new Error('recipe-service down'));
    const res = await request(app).get('/');
    expect(res.status).toBe(502);
  });
});

describe('recipe creation', () => {
  test('splits ingredients and redirects', async () => {
    backends.createRecipe.mockResolvedValue({ id: 'r9' });

    const res = await request(app)
      .post('/recipes')
      .type('form')
      .send({ name: 'fried rice', ingredients: 'rice, egg , onion', prepTimeInMinutes: '40' });

    expect(res.status).toBe(303);
    expect(backends.createRecipe).toHaveBeenCalledWith({
      name: 'fried rice',
      ingredients: ['rice', 'egg', 'onion'],
      prepTimeInMinutes: 40
    });
  });
});

describe('output encoding', () => {
  test('escapes HTML metacharacters', () => {
    expect(esc('<script>alert(1)</script>')).toBe(
      '&lt;script&gt;alert(1)&lt;/script&gt;'
    );
  });

  test('recipe names cannot inject markup', async () => {
    backends.listRecipes.mockResolvedValue([
      { id: 'r1', name: '<img src=x onerror=alert(1)>', ingredients: [], prepTimeInMinutes: 5 }
    ]);
    backends.reviewSummary.mockResolvedValue({ reviewCount: 0, averageRating: 0 });

    const res = await request(app).get('/');
    expect(res.text).not.toContain('<img src=x');
    expect(res.text).toContain('&lt;img src=x');
  });
});

describe('security headers', () => {
  test('helmet sets protective headers', async () => {
    const res = await request(app).get('/healthz');
    expect(res.headers['x-content-type-options']).toBe('nosniff');
    expect(res.headers['content-security-policy']).toBeDefined();
    expect(res.headers['content-security-policy']).toContain("default-src 'self'");
    expect(res.headers['content-security-policy']).not.toContain('upgrade-insecure-requests');
    expect(res.headers['x-powered-by']).toBeUndefined();
  });
});
