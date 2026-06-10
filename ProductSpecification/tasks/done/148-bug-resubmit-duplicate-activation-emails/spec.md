# Task 148: Resubmit job re-processes in-flight event publications → duplicate activation emails

Type: bug
Issue: #148

## Problem

A single user registration delivers the activation email **multiple times** (3× observed), each
duplicate carrying a different valid activation token. Discovered by the Task 7 full-stack E2E run
against the real stack (Postgres + Mailpit + backend on the `fullstack` profile) — exactly the kind
of real-timing integration defect the full-stack tier exists to catch.

## Solution

`ResubmitIncompletePublicationsJob` runs every 5s (`rpm.events.resubmit.interval`; scheduling on by
default via `rpm.scheduler.enabled` `matchIfMissing=true`). Its resubmit predicate is
`publication.getPublicationDate().isAfter(now - 24h)` — an **upper age bound only** ("younger than
24h") with **no lower-bound grace period**. A publication that is seconds old and still being
processed by the async `@ApplicationModuleListener` (real SMTP to Mailpit is not instant) matches
the filter and gets resubmitted on every 5s tick until it completes → duplicate listener
invocations → duplicate activation emails (a new token per invocation via `JtiGenerator.generate()`).

Add a **grace-period lower bound**: resubmit only publications incomplete LONGER than a grace
period (comfortably exceeding normal listener completion, e.g. 1 min) AND younger than the 24h
cutoff:

```java
p -> p.getPublicationDate().isBefore(now - GRACE) && p.getPublicationDate().isAfter(now - 24h)
```

In-flight publications (younger than grace) are left alone; only genuinely stuck/failed ones are
retried. The 5s interval stays. Deterministic to test via the existing test `Clock`.

## Key Files

- `src/main/java/by/iivanov/rpm/shared/infrastructure/events/ResubmitIncompletePublicationsJob.java` — add the grace-period lower bound
- `src/main/java/by/iivanov/rpm/shared/infrastructure/events/EventResubmitProperties.java` — if the grace becomes a config property
- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/events/UserRegisteredEventListener.java` — reference (the duplicated listener)

## Reproduction

1. Boot the backend on a profile with scheduling enabled and a real (non-instant) SMTP target
   (the `fullstack` profile against Mailpit; see Task 7's `Infra-FullStack-Tests-Up`).
2. Register one user (`POST /api/admin/users`, or run the Task 7 account-lifecycle journey).
3. Watch the backend log: `UserRegisteredEventListener` logs "User registered" 3 times for the same
   login/email (threads `task-1`/`task-2`/`task-3`, ~2.5s then ~5s apart), and Mailpit receives 3
   activation emails.

Scope: backend only — `shared.infrastructure.events`. No frontend.
