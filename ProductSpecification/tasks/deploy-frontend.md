# Task brief: Deploy frontend start page (Render)

> Lightweight brief. The executable task (numbered folder + `progress.md`) will be
> created later via the `/task` skill in a dedicated branch. This file captures the
> requirement and the investigation so that work isn't repeated.

## Goal

After merging to `main` and deploying, the Render web service must serve the Vue
frontend (currently the simple `HomePage` at `/`) — not just the backend API.

## Current state (why the start page is NOT served today)

- `infra/render.yaml` defines a single `web` service `rpm-ddd`, `runtime: image`,
  pulling `ghcr.io/is-ivanov/rpm-ddd:latest`.
- `Dockerfile` builds and ships **only the Spring Boot backend JAR** (`./mvnw package`
  over `src/`, Spring Boot layers, `java -jar app.jar`, `EXPOSE 10000`).
- The `frontend/` Vite app is **not** built, **not** bundled into the image, and Spring
  Boot does **not** serve any SPA static assets.
- Result: the Render URL returns backend responses; `/` likely returns `401`/`404`
  because of the allow-list (deny-by-default) security policy — `/` is not permitted.

## Options

### Option A — Spring Boot serves the frontend (single Render service) — RECOMMENDED for the free plan
- Build the Vue app (`npm run build`) and place `dist/` into `src/main/resources/static/`.
- Wire the frontend build into Maven (e.g. `frontend-maven-plugin` / `exec-maven-plugin`)
  so `dist` is produced during `./mvnw package` and included in the JAR.
- Add an SPA fallback to `index.html` and allow-list static assets in `SecurityConfig`
  (permit `GET /`, `/assets/**`, `/favicon.svg`, etc.).
- Pros: one URL, one service, no CORS — fits the Render free plan.
- Cons: couples frontend build into the backend image; slower image build.

### Option B — Separate Render Static Site for the frontend
- New Render service of type **Static Site**: build `cd frontend && npm install && npm run build`,
  publish `frontend/dist`.
- `VITE_API_URL` points at the backend service URL.
- Needs CORS configuration on the backend.
- Pros: clean SPA/API separation; independent deploys.
- Cons: two services + CORS; second deploy target to manage.

## Open questions (resolve during `/task`)
- Confirm Option A vs B.
- For A: which Maven plugin and Node/npm provisioning in the Docker build stage.
- Security allow-list entries for static assets and SPA fallback.
- CI: does the GHCR image build trigger on merge to `main`? (see `.github/`)

## Affected layers (preliminary)
- Infrastructure: `Dockerfile`, `infra/render.yaml`, CI workflow.
- Backend: `pom.xml` (frontend build), `SecurityConfig` (allow-list), static resource / SPA fallback.
- Frontend: build output only (no feature code).
