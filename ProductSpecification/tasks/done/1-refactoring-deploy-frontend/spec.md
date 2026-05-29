# Task 1: Deploy frontend start page (Render)

Type: refactoring

## Problem

After merging to `main` and deploying, the Render web service serves only the Spring
Boot backend API — the Vue start page (`HomePage` at `/`) is not reachable in the web.

Root cause (verified):
- `infra/render.yaml` runs a single `web` service `rpm-ddd` from image
  `ghcr.io/is-ivanov/rpm-ddd:latest`.
- `.github/workflows/deploy.yml` builds that image from the **pre-built `app.jar`
  artifact** (via `Dockerfile.deploy`) produced by the "Java CI with Maven" workflow
  (`build.yml` → `mvn verify -B`). It does not run Maven or build the frontend.
- The `frontend/` Vite app is never built or bundled; Spring Boot serves no SPA static
  assets. `SecurityConfig` ends with `anyRequest().denyAll()`, so `/` is denied (401).

## Solution

Option A — Spring Boot serves the SPA from a single Render service:

1. Build the Vue app during the Maven build and bundle `frontend/dist` into the JAR as
   static resources (so the existing `Dockerfile.deploy` → GHCR → Render flow ships it
   unchanged — no Dockerfile edit needed).
2. Allow-list static assets in `SecurityConfig` (deny-by-default preserved) and add an
   SPA fallback so client-side routes resolve to `index.html` while `/api/**` stays
   protected.
3. Make CI build the frontend: add `frontend/**` to the `paths:` triggers in `build.yml`
   so frontend changes trigger the JAR build, and ensure `mvn verify` runs the frontend
   build on the CI runner.

Decisions taken at task creation:
- Approach: **Option A** (single service; no CORS).
- The deployed image path uses `Dockerfile.deploy` + the JAR artifact, so the frontend
  must be inside the JAR (built by Maven), not by a Node stage in `Dockerfile`.
- The frontend build lives behind an **explicit `frontend` Maven profile** (NOT active
  by default), activated with `-Pfrontend`. Rationale: everyday `./mvnw test` stays fast,
  and the Alpine fallback `Dockerfile` (plain `mvn package`) skips the frontend by design
  (avoids the glibc-Node-on-musl problem). CI (`build.yml`) must pass `-Pfrontend`.

## Key Files

- `pom.xml` — add frontend build (e.g. `frontend-maven-plugin`: it downloads its own
  Node/npm, runs `npm ci` + `npm run build`) + copy `frontend/dist` → `target/classes/static`.
- `src/main/java/by/iivanov/rpm/iam/auth/infrastructure/SecurityConfig.java` —
  allow-list `GET /`, `/index.html`, `/assets/**`, `/favicon.svg`, and SPA routes;
  keep `/api/**` authenticated and `anyRequest().denyAll()`.
- SPA fallback — a `WebMvcConfigurer`/resource resolver or forwarding controller that
  serves `index.html` for non-`/api`, non-asset GET routes.
- `.github/workflows/build.yml` — add `frontend/**` to push/PR `paths:`; ensure the
  Maven build produces a JAR containing `static/index.html`.
- `infra/render.yaml`, `Dockerfile.deploy` — expected unchanged; verify only.

## Open questions / risks

- `frontend-maven-plugin` downloads a glibc Node binary; the fallback full `Dockerfile`
  uses Alpine (musl). The primary deploy path (`Dockerfile.deploy` + artifact) is
  unaffected, but if the full `Dockerfile` build path is ever used it may need a Node
  base or `-Dskip.frontend`. Decide whether to guard the plugin behind a profile.
- Confirm the SPA fallback does not shadow the error-handling for unknown `/api/**`
  routes (those must still return the RFC 9457 problem detail, not `index.html`).
- Confirm Render auto-deploys on new `:latest` (the workflow also POSTs the Render
  deploy hook).
