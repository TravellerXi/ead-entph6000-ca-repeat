'use strict';

const express = require('express');
const helmet = require('helmet');
const backends = require('./backends');

const INSTANCE_COLOUR = process.env.INSTANCE_COLOUR || 'blue';

/** Escapes user-supplied values before they reach the HTML response. */
function esc(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function layout(title, content) {
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${esc(title)}</title>
  <link rel="stylesheet" href="/static/style.css">
</head>
<body>
  <header>
    <h1>EAD Recipe Platform</h1>
    <span class="badge badge-${esc(INSTANCE_COLOUR)}">${esc(INSTANCE_COLOUR)}</span>
  </header>
  <nav><a href="/">Recipes</a><a href="/users">Users</a></nav>
  <main>${content}</main>
  <footer>TU Dublin &mdash; Enterprise Architecture Deployment CA Repeat</footer>
</body>
</html>`;
}

function createApp() {
  const app = express();

  app.use(helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        // The assessment endpoint is an HTTP-only LoadBalancer IP. Helmet's
        // default upgrade directive rewrites its own CSS request to HTTPS and
        // leaves the live UI unstyled; all other CSP protections remain active.
        'upgrade-insecure-requests': null,
      },
    },
  }));
  app.use(express.urlencoded({ extended: false, limit: '32kb' }));
  app.use(express.json({ limit: '32kb' }));
  app.use('/static', express.static(`${__dirname}/../public`, { maxAge: '1h' }));

  // --- Kubernetes probes: liveness must not depend on backends, readiness may. ---
  app.get('/healthz', (_req, res) => res.status(200).json({ status: 'UP' }));

  app.get('/readyz', async (_req, res) => {
    try {
      await backends.recipeMeta();
      res.status(200).json({ status: 'READY' });
    } catch (err) {
      res.status(503).json({ status: 'NOT_READY', reason: err.message });
    }
  });

  app.get('/_meta', (_req, res) =>
    res.json({ service: 'frontend', instanceColour: INSTANCE_COLOUR }));

  // --- Recipes ---
  app.get('/', async (_req, res) => {
    try {
      const recipes = await backends.listRecipes();
      const rows = await Promise.all(
        recipes.map(async (r) => {
          let summary = { reviewCount: 0, averageRating: 0 };
          try {
            summary = await backends.reviewSummary(r.id);
          } catch {
            // Review service degradation must not blank the recipe list.
          }
          return `<tr>
            <td>${esc(r.name)}</td>
            <td>${esc((r.ingredients || []).join(', '))}</td>
            <td>${esc(r.prepTimeInMinutes)} min</td>
            <td>${esc(summary.averageRating)} (${esc(summary.reviewCount)})</td>
            <td><form method="post" action="/recipes/${encodeURIComponent(r.id)}/delete">
              <button class="danger" type="submit">Delete</button></form></td>
          </tr>`;
        })
      );

      res.send(layout('Recipes', `
        <h2>Recipes</h2>
        <table>
          <thead><tr><th>Name</th><th>Ingredients</th><th>Prep</th><th>Rating</th><th></th></tr></thead>
          <tbody>${rows.join('') || '<tr><td colspan="5">No recipes yet.</td></tr>'}</tbody>
        </table>
        <h2>Add a recipe</h2>
        <form method="post" action="/recipes">
          <label>Name <input name="name" required maxlength="80"></label>
          <label>Ingredients (comma separated) <input name="ingredients" required></label>
          <label>Prep time (minutes) <input name="prepTimeInMinutes" type="number" min="1" required></label>
          <button type="submit">Create</button>
        </form>`));
    } catch (err) {
      res.status(502).send(layout('Error',
        `<p class="error">Recipe service unavailable: ${esc(err.message)}</p>`));
    }
  });

  app.post('/recipes', async (req, res) => {
    try {
      await backends.createRecipe({
        name: req.body.name,
        ingredients: String(req.body.ingredients || '').split(',').map((s) => s.trim()).filter(Boolean),
        prepTimeInMinutes: Number(req.body.prepTimeInMinutes)
      });
      res.redirect(303, '/');
    } catch (err) {
      res.status(err.status || 502).send(layout('Error',
        `<p class="error">Could not create recipe: ${esc(err.message)}</p><a href="/">Back</a>`));
    }
  });

  app.post('/recipes/:id/delete', async (req, res) => {
    try {
      await backends.deleteRecipe(req.params.id);
      res.redirect(303, '/');
    } catch (err) {
      res.status(err.status || 502).send(layout('Error',
        `<p class="error">Could not delete recipe: ${esc(err.message)}</p><a href="/">Back</a>`));
    }
  });

  // --- Users ---
  app.get('/users', async (_req, res) => {
    try {
      const users = await backends.listUsers();
      const rows = users.map((u) =>
        `<tr><td>${esc(u.username)}</td><td>${esc(u.email)}</td><td>${esc(u.displayName)}</td></tr>`).join('');
      res.send(layout('Users', `
        <h2>Users</h2>
        <table>
          <thead><tr><th>Username</th><th>Email</th><th>Display name</th></tr></thead>
          <tbody>${rows || '<tr><td colspan="3">No users yet.</td></tr>'}</tbody>
        </table>
        <h2>Register</h2>
        <form method="post" action="/users">
          <label>Username <input name="username" required minlength="3" maxlength="40"></label>
          <label>Email <input name="email" type="email" required></label>
          <label>Display name <input name="displayName"></label>
          <label>Password <input name="password" type="password" required minlength="8"></label>
          <button type="submit">Register</button>
        </form>`));
    } catch (err) {
      res.status(502).send(layout('Error',
        `<p class="error">User service unavailable: ${esc(err.message)}</p>`));
    }
  });

  app.post('/users', async (req, res) => {
    try {
      await backends.registerUser({
        username: req.body.username,
        email: req.body.email,
        displayName: req.body.displayName,
        password: req.body.password
      });
      res.redirect(303, '/users');
    } catch (err) {
      res.status(err.status || 502).send(layout('Error',
        `<p class="error">Could not register user: ${esc(err.message)}</p><a href="/users">Back</a>`));
    }
  });

  app.use((_req, res) => res.status(404).send(layout('Not found', '<p>Not found.</p>')));

  return app;
}

module.exports = { createApp, esc };
