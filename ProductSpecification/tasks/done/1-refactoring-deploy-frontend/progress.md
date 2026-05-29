# Task 1: Deploy frontend start page (Render) — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Build the Vue frontend in the Maven build and bundle it into the JAR
- [x] wire frontend build into pom.xml behind an explicit `frontend` profile (frontend-maven-plugin: install-node-and-npm + `npm ci` + `npm run build` on `frontend/`) and copy `frontend/dist` → `${project.build.outputDirectory}/static`
- [x] verify: `./mvnw -Pfrontend -DskipTests package` produces a JAR containing `BOOT-INF/classes/static/index.html` and `static/assets/**`

### Step 2: Serve the SPA from Spring Boot (allow-list + fallback)
- [x] red: integration test — `GET /` → 200 `text/html` (index.html), a deep link (`GET /login`) → SPA shell, `GET /api/**` (unauthenticated) → still 401 (confirmed: 2 fail @401 vs 200, 1 pass)
- [x] green: SecurityConfig allow-lists `GET /,/index.html,/favicon.svg,/assets/**,/login,/activate`; `SpaForwardingController` forwards explicit SPA routes to `index.html`; `/api/**` stays authenticated + `anyRequest().denyAll()` (3 tests pass)
- [x] refactor (cleanup): merged the two SPA-shell tests into a `@ParameterizedTest`; checkstyle 0 / pmd clean

### Step 3: Make CI build the frontend and ship it
- [x] add `-Pfrontend` to the `mvn verify` step in `.github/workflows/build.yml` (profile is NOT active by default)
- [x] add `frontend/**` to `paths:` (push + pull_request) in `.github/workflows/build.yml`
- [x] verified locally (Step 1) that `mvn -Pfrontend package` bundles the static SPA into the JAR — CI uses the same path; `Dockerfile.deploy` / `infra/render.yaml` unchanged (deploy builds from the `app-jar` artifact; the Alpine fallback `Dockerfile` runs plain `mvn package` → skips frontend by design). Full CI verification happens on the merge-to-main run.

### Step 4: Verify deploy
- [x] local: built JAR with `-Pfrontend`, ran `java -jar` against a throwaway Postgres. Verified: `GET /` → 200 text/html (real built `index.html` referencing `/assets/index-*.js|css`, `/favicon.svg`); `GET /login` → 200 (SPA fallback); `GET /assets/index-*.js` → 200 text/javascript; `GET /api/auth/csrf` → 200; `GET /api/auth/me` (unauth) → 401.
- [x] deploy chain documented (below)

## Deploy chain (Option A)

On merge to `main`:
1. `build.yml` ("Java CI with Maven") runs `mvn verify -B -Pfrontend` → frontend-maven-plugin builds `frontend/dist` and bundles it into the JAR under `static/`; uploads the `app-jar` artifact.
2. `deploy.yml` downloads `app-jar`, wraps it via `Dockerfile.deploy`, pushes `ghcr.io/is-ivanov/rpm-ddd:latest`, then POSTs the Render deploy hook.
3. Render redeploys the single `rpm-ddd` web service → the Vue start page is served at the service root; `/api/**` stays authenticated.

No change needed to `Dockerfile.deploy` or `infra/render.yaml`. The Alpine fallback `Dockerfile` (plain `mvn package`, no `-Pfrontend`) intentionally ships a backend-only image.
