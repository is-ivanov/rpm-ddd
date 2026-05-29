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
- [~] green: SecurityConfig allow-list static assets + SPA fallback (forward non-`/api` non-asset routes to `index.html`), keep `/api/**` authenticated and `anyRequest().denyAll()`
- [ ] refactor (cleanup) + run affected tests

### Step 3: Make CI build the frontend and ship it
- [ ] add `-Pfrontend` to the `mvn verify` step in `.github/workflows/build.yml` (profile is NOT active by default)
- [ ] add `frontend/**` to `paths:` (push + pull_request) in `.github/workflows/build.yml`
- [ ] verify `mvn verify -Pfrontend` builds the frontend on the runner and the uploaded `app-jar` contains the static SPA; confirm `Dockerfile.deploy` / `infra/render.yaml` need no change (the Alpine fallback `Dockerfile` runs plain `mvn package` → skips frontend by design)

### Step 4: Verify deploy
- [ ] local: build JAR, run `java -jar`, `curl /` returns the Vue page; `GET /api/auth/csrf` still works
- [ ] document deploy verification (merge → CI builds JAR+image → GHCR `:latest` → Render redeploy → start page live)
