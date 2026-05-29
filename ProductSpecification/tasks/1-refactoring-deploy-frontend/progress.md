# Task 1: Deploy frontend start page (Render) — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Build the Vue frontend in the Maven build and bundle it into the JAR
- [~] wire frontend build into pom.xml (frontend-maven-plugin: npm ci + npm run build on `frontend/`) and copy `frontend/dist` → `target/classes/static`
- [ ] verify: `./mvnw -DskipTests package` produces a JAR containing `static/index.html` and `static/assets/**`

### Step 2: Serve the SPA from Spring Boot (allow-list + fallback)
- [ ] red: integration test — `GET /` → 200 `text/html` (index.html), a deep link (e.g. `GET /login`) → SPA shell, `GET /api/**` (unauthenticated) → still 401
- [ ] green: SecurityConfig allow-list static assets + SPA fallback (forward non-`/api` non-asset routes to `index.html`), keep `/api/**` authenticated and `anyRequest().denyAll()`
- [ ] refactor (cleanup) + run affected tests

### Step 3: Make CI build the frontend and ship it
- [ ] add `frontend/**` to `paths:` (push + pull_request) in `.github/workflows/build.yml`
- [ ] verify `mvn verify` builds the frontend on the runner and the uploaded `app-jar` contains the static SPA; confirm `Dockerfile.deploy` / `infra/render.yaml` need no change

### Step 4: Verify deploy
- [ ] local: build JAR, run `java -jar`, `curl /` returns the Vue page; `GET /api/auth/csrf` still works
- [ ] document deploy verification (merge → CI builds JAR+image → GHCR `:latest` → Render redeploy → start page live)
