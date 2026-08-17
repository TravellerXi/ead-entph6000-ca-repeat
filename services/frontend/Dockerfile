# ---- Dependency stage ----
FROM node:22-alpine AS deps
WORKDIR /build
COPY package.json package-lock.json* ./
RUN npm ci --omit=dev || npm install --omit=dev

# ---- Runtime stage ----
FROM node:22-alpine
# Dependencies are installed in the deps stage, so the runtime never invokes npm.
# Removing it drops npm's bundled tar, whose CVE-2026-59873 is the only CRITICAL
# finding Trivy reports against this image.
RUN rm -rf /usr/local/lib/node_modules/npm /usr/local/bin/npm /usr/local/bin/npx
RUN addgroup -S app && adduser -S -G app -u 10001 app
WORKDIR /app
COPY --from=deps --chown=app:app /build/node_modules ./node_modules
COPY --chown=app:app package.json ./
COPY --chown=app:app src ./src
COPY --chown=app:app public ./public
USER 10001
ENV NODE_ENV=production PORT=3000
EXPOSE 3000
CMD ["node", "src/server.js"]
