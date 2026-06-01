# Story 2: Email integration — send email on user registration — Progress

## Spec
- [x] interview
- [x] story
- [S] mockups (backend/integration story — no UI; activation link targets Story 1's existing frontend page)
- [S] api-spec (no new HTTP endpoints — email is an async side-effect of existing registration flow; `/activate` endpoints exist in Story 1)
- [x] test-spec

## Backend Scenarios

### Scenario 1.1: Registering a user delivers an activation email
- [x] red-acceptance
- [x] design
- [S] red-usecase (listener→port flow reused unchanged from Story 1; zero usecase/application files change — see ADR test-layering)
- [S] green-usecase (no usecase/application production code — UserRegisteredEventListener already calls EmailNotificationSender.sendActivationToken)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
  - Check 1 (ports): email — only NoOpEmailNotificationSender (logs, no real send); Mailpit acceptance needs SmtpEmailNotificationSender + ActivationEmailRenderer → red/green-adapter email
  - Check 2 (exceptions): [S] — listener happy-path, no domain exceptions to map
  - Check 3 (response shape): [S] — inbound adapter is an event listener, no HTTP response
- [x] red-adapter email
- [x] green-adapter email
- [x] green-acceptance

## Integration Scenarios

### Scenario 6.1: A successful registration delivers exactly one activation email
- [x] red-acceptance
- [x] design
- [S] red-usecase (resubmit is pure Modulith infra; zero usecase/application files change — see design)
- [S] green-usecase (no usecase/application production code — ResubmitIncompletePublicationsJob delegates to IncompleteEventPublications)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
  - Check 1 (ports): [S] — only collaborator is the Modulith-provided IncompleteEventPublications bean (framework infra, not our adapter); resubmit() is simple delegation
  - Check 2 (exceptions): [S] — resubmit happy-path, no domain exceptions to map
  - Check 3 (response shape): [S] — resubmit job invoked directly, no HTTP response; simple delegation → body created in green-acceptance
- [x] green-acceptance

### Scenario 7.1: Incomplete publications older than 24 hours are not resubmitted
- [x] red-acceptance
- [x] design (see ADR resubmit-job-placement: relocate job to shared + predicate age cutoff)
- [S] red-usecase (cutoff lives in the infra events adapter; zero usecase/application files change — mirrors 6.1)
- [S] green-usecase (no usecase/application production code — age cutoff is in ResubmitIncompletePublicationsJob)
- [S] red-domain
- [S] green-domain
- [x] refactor (relocate ResubmitIncompletePublicationsJob to shared.infrastructure.events — see ADR)
- [x] adapters-discovery
  - Check 1 (ports): [S] — collaborators are the Modulith `IncompleteEventPublications` bean (framework infra) and the existing `Clock` (shared.time.infrastructure.ClockConfiguration); no new adapter — cutoff predicate is minimal job code created in green-acceptance
  - Check 2 (exceptions): [S] — resubmit happy-path, no domain exceptions to map
  - Check 3 (response shape): [S] — resubmit job invoked directly, no HTTP response; simple delegation → age-cutoff predicate created in green-acceptance
- [x] green-acceptance

### Scenario 8.1: The resubmit scheduler is wired, scheduled, and lock-guarded in production
> Reopened: 6.1/7.1 invoked `resubmitJob.resubmit()` directly, so the `@Scheduled` wiring was never under test and is missing in production — the job never fires on the deployed app. Verified by a fast `ApplicationContextRunner` wiring test (NOT an awaited tick). Decisions in ADR `resubmit-scheduling-wiring`: `@Scheduled(fixedDelayString="${rpm.events.resubmit.interval}")`; app-wide `SchedulingConfiguration` gated by `rpm.scheduler.enabled` (false in `test`); ShedLock for multi-instance. **Design ran before red here** — the wiring test references production types whose shape is an architectural decision.
- [x] design (ADR `resubmit-scheduling-wiring`: fixedDelay + required interval property, app-wide gated `SchedulingConfiguration`, ShedLock `@SchedulerLock`/`LockProvider` + `shedlock` migration, add shedlock deps to `pom.xml`)
- [S] red-acceptance (no black-box/HTTP acceptance for scheduler wiring; realized as `red-adapter scheduling` — the `ApplicationContextRunner` wiring test, mirroring how Security 5.1 realized its check at adapter level)
- [S] red-usecase (scheduler wiring is pure infra — zero usecase/application files change, mirrors 6.1/7.1)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
  - Check 1 (ports): scheduling — the job's scheduling wiring (`SchedulingConfiguration` with `@EnableScheduling`/`@EnableSchedulerLock`/`LockProvider`, `@Scheduled(fixedDelayString)`/`@SchedulerLock` on the job, `EventResubmitProperties`) does not exist → red/green-adapter scheduling. Existing collaborators `IncompleteEventPublications` (Modulith) + `Clock` (shared.time) are provided.
  - Check 2 (exceptions): [S] — resubmit happy-path, no domain exceptions to map
  - Check 3 (response shape): [S] — scheduled trigger, no HTTP response
- [~] red-adapter scheduling (`ApplicationContextRunner` wiring test — RED: no `SchedulingConfiguration`/`@Scheduled`/ShedLock yet; asserts context boots, `LockProvider` present, `EventResubmitProperties.interval()` == 5s)
- [ ] green-adapter scheduling (implement per ADR `resubmit-scheduling-wiring`: `SchedulingConfiguration` + `EventResubmitProperties` + `@Scheduled(fixedDelayString)`/`@SchedulerLock` on the job + ShedLock deps + `JdbcTemplateLockProvider` + `shedlock` Liquibase changeset + `application.yml`/`application-test.yml`. **VERIFY ShedLock × Spring Boot 4 / Spring 7 compat FIRST** — advisory-lock fallback if incompatible)
- [S] green-acceptance (wiring covered at adapter level; no separate acceptance test)

## Frontend Scenarios
(none — no UI in this story)

## Security Scenarios

### Scenario 5.1: User-controlled login is escaped in the rendered email
- [S] red-acceptance (rendered-content verification → fast renderer test, NOT e2e/Mailpit — see tdd-rules "rendered-content verification" + 05_Security_Tests.md §5 note; realized as red-adapter email)
- [S] design (no architectural change — extends existing ActivationEmailRenderer; fix is HTML-escaping user-controlled login on HTML fill, anticipated by email-render-boundary ADR)
- [S] red-usecase (no usecase/application files — escaping lives in the notification adapter renderer)
- [S] green-usecase (no usecase/application production code — fix is in ActivationEmailRenderer)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery
  - Check 1 (ports): [S] — no new port; existing email adapter `ActivationEmailRenderer` (notification) gains escaping; test extends `ActivationEmailRendererTest` → red/green-adapter email
  - Check 2 (exceptions): [S] — pure rendering, no domain exceptions to map
  - Check 3 (response shape): [S] — renderer returns `ActivationEmailContent` value, no HTTP response
- [x] red-adapter email
- [x] green-adapter email
- [S] green-acceptance (rendered-content verified at adapter level; spec forbids pinning rendered content into e2e)

## Load Scenarios
(none — negligible email volume, async send; see tests/03_Load_Tests.md)

## Infrastructure Scenarios

### Scenario 4.1: SMTP unavailable does not fail registration
- [S] red-acceptance (behavior already covered by `StaleIncompletePublicationIntegrationTest` (7.1): with the SMTP spy armed to fail, that test already asserts registration returns 201 + no email delivered + the publication stays incomplete. The async `@ApplicationModuleListener` never propagates the send failure to the HTTP response — zero production code needed. Avoid a second slow full-context test for behavior already asserted.)
- [S] design (no architectural change — resilience emerges from the existing async listener; see resubmit-job-placement ADR)
- [S] red-usecase (no usecase/application files — async send failure is swallowed by the Modulith listener)
- [S] green-usecase (no usecase/application production code)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (no new ports — existing `SmtpEmailNotificationSender` + Modulith publication registry already cover the behavior)
- [S] green-acceptance (covered by 7.1; no separate acceptance test added)

### Scenario 5.1: Activation email is delivered after SMTP recovers
- [S] red-acceptance (feature already implemented — resubmit pipeline proven by 6.1/7.1; zero production files change. New acceptance test `SmtpRecoveryEmailDeliveryIntegrationTest` passes on first run, so there is no red state to capture. The behavior — a *young* (< 24h) failed publication is redelivered on resubmit after SMTP recovery — is NOT asserted by any existing test, so the test is genuine non-redundant coverage and is retained, committed under green-acceptance.)
- [S] design (no architectural change — recovery emerges from the existing resubmit scheduler within the 24h window; see resubmit-job-placement ADR)
- [S] red-usecase (no usecase/application files — resubmit + listener redelivery is Modulith infra reused unchanged)
- [S] green-usecase (no usecase/application production code)
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (no new ports — existing `ResubmitIncompletePublicationsJob` + `SmtpEmailNotificationSender` + Modulith registry cover the behavior)
- [x] green-acceptance (`SmtpRecoveryEmailDeliveryIntegrationTest` committed; passes 1/1)

### Scenario 7.1: The application context starts with the production mail configuration
> Reopened: tests run under the `test` profile (Mailpit supplies `JavaMailSender`); `prod` had no `spring.mail.*`, so the unconditional `@Primary SmtpEmailNotificationSender` cannot construct and the context fails to start on Render.
- [ ] red-acceptance
- [ ] design (`/architecture` ADR: (1) sender selection — real SMTP when configured vs fail-fast when *prod* mail absent; (2) local-dev mail — docker SMTP vs `local`-profile file-writing `JavaMailSender` (body `.html`/text + metadata `.json`) vs `NoOp`; replaces the unconditional NoOp+Smtp coexistence)
- [S] red-usecase (context/config bootstrap — no usecase/application change)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance
