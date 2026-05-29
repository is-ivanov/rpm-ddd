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
- [ ] local: build JAR, run `java -jar`, `curl /` returns the Vue page; `GET /api/auth/csrf` still works
- [ ] document deploy verification (merge → CI builds JAR+image → GHCR `:latest` → Render redeploy → start page live)
