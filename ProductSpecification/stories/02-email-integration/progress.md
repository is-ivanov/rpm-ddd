# Story 2: Email integration — send email on user registration — Progress

> Terse entries (status + test-class/ADR ref + `see summaries/X` link). The "why" lives in
> `summaries/` + `carryover.md`; see `.claude/rules/workflow.md` → "Updating Progress".

## Spec
- [x] interview
- [x] story
- [S] mockups (backend/integration story — no UI)
- [S] api-spec (no new HTTP endpoints — async side-effect of existing registration flow)
- [x] test-spec

## Backend Scenarios

### Scenario 1.1: Registering a user delivers an activation email
- [x] red-acceptance
- [x] design
- [S] red-usecase (listener→port flow reused unchanged from Story 1)
- [S] green-usecase (UserRegisteredEventListener already calls EmailNotificationSender)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (email: Mailpit needs SmtpEmailNotificationSender + ActivationEmailRenderer)
- [x] red-adapter email
- [x] green-adapter email
- [x] green-acceptance

## Integration Scenarios

### Scenario 6.1: A successful registration delivers exactly one activation email
- [x] red-acceptance
- [x] design
- [S] red-usecase (resubmit is pure Modulith infra)
- [S] green-usecase (ResubmitIncompletePublicationsJob delegates to IncompleteEventPublications)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (simple delegation to Modulith IncompleteEventPublications; body in green-acceptance)
- [x] green-acceptance

### Scenario 7.1: Incomplete publications older than 24 hours are not resubmitted
- [x] red-acceptance
- [x] design (ADR resubmit-job-placement: relocate job to shared + predicate age cutoff)
- [S] red-usecase (cutoff lives in infra events adapter)
- [S] green-usecase (age cutoff in ResubmitIncompletePublicationsJob)
- [S] red-domain
- [S] green-domain
- [x] refactor (relocate ResubmitIncompletePublicationsJob to shared.infrastructure.events)
- [x] adapters-discovery (age-cutoff predicate created in green-acceptance)
- [x] green-acceptance

### Scenario 8.1: The resubmit scheduler is wired, scheduled, and lock-guarded in production
> Reopened: 6.1/7.1 invoked the job directly, so `@Scheduled` wiring was never under test and the
> job never fired in production. ADR resubmit-scheduling-wiring. See summaries/8-1-scheduler-wiring.md.
- [x] design (ADR resubmit-scheduling-wiring: fixedDelay + required interval, gated SchedulingConfiguration, ShedLock)
- [S] red-acceptance (realized as red-adapter scheduling)
- [S] red-usecase (scheduler wiring is pure infra)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (scheduling: SchedulingConfiguration + EventResubmitProperties)
- [x] red-adapter scheduling (EventResubmitSchedulingTest — ApplicationContextRunner wiring test)
- [x] green-adapter scheduling (see summaries/8-1-scheduler-wiring.md)
- [S] green-acceptance (wiring covered at adapter level)

## Frontend Scenarios
(none — no UI in this story)

## Security Scenarios

### Scenario 5.1: User-controlled login is escaped in the rendered email
- [S] red-acceptance (rendered-content verification → fast renderer test, not e2e; realized as red-adapter email)
- [S] design (extends existing ActivationEmailRenderer, anticipated by email-render-boundary ADR)
- [S] red-usecase (escaping lives in the notification adapter renderer)
- [S] green-usecase (fix is in ActivationEmailRenderer)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (extends ActivationEmailRendererTest)
- [x] red-adapter email
- [x] green-adapter email
- [S] green-acceptance (rendered-content verified at adapter level)

## Load Scenarios
(none — negligible email volume, async send; see tests/03_Load_Tests.md)

## Infrastructure Scenarios

### Scenario 4.1: SMTP unavailable does not fail registration
- [S] red-acceptance (covered by StaleIncompletePublicationIntegrationTest 7.1 — see summaries/4-1-smtp-unavailable.md)
- [S] design (resilience emerges from existing async listener; see resubmit-job-placement ADR)
- [S] red-usecase (async send failure swallowed by the Modulith listener)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (existing SmtpEmailNotificationSender + Modulith publication registry cover it)
- [S] green-acceptance (covered by 7.1)

### Scenario 5.1: Activation email is delivered after SMTP recovers
- [S] red-acceptance (resubmit pipeline already proven by 6.1/7.1; SmtpRecoveryEmailDeliveryIntegrationTest passes first run)
- [S] design (recovery emerges from existing resubmit scheduler within 24h window; see resubmit-job-placement ADR)
- [S] red-usecase (resubmit + listener redelivery is Modulith infra reused unchanged)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery (existing ResubmitIncompletePublicationsJob + SmtpEmailNotificationSender + Modulith registry cover it)
- [x] green-acceptance (SmtpRecoveryEmailDeliveryIntegrationTest committed; 1/1)

### Scenario 7.1: The application context starts with the production mail configuration
> Reopened: tests run under `test` profile (Mailpit supplies JavaMailSender); `prod` had no
> `spring.mail.*`, so context failed to start on Render. See summaries/7-1-prod-mail-bootstrap.md.
- [x] design (ADR production-mail-bootstrap: prod fail-fast SPRING_MAIL_* env, sole conditional sender, MVP = Gmail SMTP)
- [x] red-acceptance (sliced ApplicationContextRunner ProductionMailBootstrapTest, mirrors EventResubmitSchedulingTest)
- [S] red-usecase (context/config bootstrap — no usecase/application change)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (email: config/wiring scenario, no usecase/ports → green-adapter email)
- [x] green-adapter email (per ADR production-mail-bootstrap — see summaries/7-1-prod-mail-bootstrap.md)
- [x] red-adapter email (coverage: buildMessage wraps MessagingException as IllegalStateException)
- [x] green-adapter email (coverage: already-implemented branch, no-op pass; SmtpEmailNotificationSender 100% covered)
- [S] green-acceptance (wiring covered at adapter level)
