# Task 215: Show deployed app version in the header

Type: feature
Issue: #215  <- for numbering/traceability only; tests are NOT issue-tagged (tagging is a
              bug-task convention per tech binding `tdd.md` — a feature is not a regression guard)

> Note: the `/task` framework natively defines `bug` and `refactoring` types. This is a small
> standalone feature the user chose to run as a lightweight task rather than a full story, so
> it uses `Type: feature` with an adapted backend+frontend progress sequence.

## Problem

There is no way to tell which build is currently deployed from the running app. We need a
visible, low-friction way to read the deployed version (and git commit / build time) from
the UI — e.g. to confirm a deploy actually shipped the expected code.

## Solution

**Build (Maven)**
- `spring-boot-maven-plugin` `build-info` goal → `build-info.properties` (version, build time).
- `git-commit-id-maven-plugin` → `git.properties` (short SHA, branch). CI checkout has
  `.git`, so the metadata is baked into the JAR.

**Backend (Actuator)**
- Add `spring-boot-starter-actuator`.
- Expose **only** `info`: `management.endpoints.web.exposure.include=info` (health/env/beans
  stay closed). `management.info.git.mode=full`, `management.info.build.enabled=true`.
- Security (deny-by-default allow-list): permit **authenticated** `GET /actuator/info`
  (the help icon lives in the logged-in app shell); deny the rest of `/actuator/**`.
  `/actuator/health` is enabled too (consumed by Task #214's Render health check).

**Frontend**
- API client `getAppInfo()` → `GET /actuator/info`, validating the payload at the boundary
  (humble object) into a typed shape.
- Header: a `HelpCircle` (lucide) icon next to the user avatar, with `data-testid`.
  Clicking opens a popover showing `version · commit · build time`.
- The version fetch is an async action → the popover shows a loading state while fetching.

## Key Files

- `pom.xml` — actuator dependency, build-info + git-commit-id plugins
- `src/main/resources/application.yml` — `management.*` exposure/info config
- backend security config — allow-list entry for authenticated `GET /actuator/info`
- `frontend/src/...` — header component (icon + popover), `getAppInfo` API client, tests

## Design Notes

- Rationale (header help icon, not footer): industry apps don't surface build version in a
  footer; a `?`-style info affordance in the header is the chosen pattern.
- No dedicated mockup folder (lightweight task) — the popover is small; `align-design`
  produces the visual against a minimal inline reference.

## Full-Stack Journey Verdict

**no-impact.** The header version popover is a read-only auxiliary widget that reads
`GET /actuator/info`; it sits off the account-lifecycle spine (admin login → create user →
activation email → activate → new-user login) and changes no step of the critical
user-lifecycle path. Asserting it in the journey would be an edge affordance, which the
browser-testing binding explicitly keeps in the fast mocked suite (covered by
`app-version.spec.ts`), never on the growing journey. No `*.fullstack.spec.ts` change.
