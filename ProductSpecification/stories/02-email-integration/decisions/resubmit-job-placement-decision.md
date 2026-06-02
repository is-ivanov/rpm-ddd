# Decision: Resubmit scheduler lives in `shared`, age-cutoff via predicate

**Date**: 2026-06-01 **Scenarios**: 7.1

The 24h age cutoff forces a choice of resubmit API; revisiting the job exposed that it operates app-wide yet sits inside the `iam.user` module.

| Rejected | Why |
|----------|-----|
| Keep `ResubmitIncompletePublicationsJob` in `iam.user.infrastructure.events` | `IncompleteEventPublications.resubmitIncompletePublications` scans the **whole** Modulith registry (every module's publications), so the job's reach is app-wide; burying it in the user subdomain is a module-boundary smell that worsens once a second module publishes events. |
| `resubmitIncompletePublicationsOlderThan(Duration)` | Resubmits publications **older** than the duration — the inverse of the requirement (we must resubmit only those *younger* than 24h and skip the stale ones). |
| Externalize 24h to an `@ConfigurationProperties` object | Single standalone value sharing no concern with other settings — config-grouping rule allows it to stay an inline constant; externalizing is premature. |

**Chosen**: Relocate the job to `by.iivanov.rpm.shared.infrastructure.events` (the `OPEN` shared module that already hosts cross-cutting infra such as `ClockConfiguration`), generalize its Javadoc (drop the activation-email framing), and implement the cutoff with a predicate over `EventPublication.getPublicationDate()`. The `UserRegisteredEventListener` stays in `iam.user` — it is genuinely user-specific.

## Model

- `shared.infrastructure.events.ResubmitIncompletePublicationsJob` — moved from `iam.user.infrastructure.events`; gains a `Clock` collaborator.
- `resubmit()` — `cutoff = clock.instant().minus(Duration.ofHours(24))`; resubmits publications whose `getPublicationDate().isAfter(cutoff)`.
- Clock basis: the registry stamps `getPublicationDate()` with the injected `Clock` bean (`shared.time.infrastructure.ClockConfiguration`), so the job's "now" and the publication timestamp share one clock — the test's `MutableClock` advance makes a publication observably stale.

## Edge Cases

| Case | Behavior |
|------|----------|
| Publication age exactly 24h | `isAfter(now - 24h)` is false → excluded (treated as stale). |
| Publication younger than 24h | Resubmitted (within window). |
