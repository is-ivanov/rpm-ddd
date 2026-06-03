# Task 5: Evaluate GreenMail as alternative to Mailpit for SMTP integration tests — Progress

Type: refactoring

Tracks GitHub issue #98.

## Spec
- [x] spec

## Fix

### Step 1: Spike — evaluate GreenMail vs Mailpit & decide
- [ ] spike — measure latency for the 3 modes (GreenMail in-JVM, GreenMail Docker/Testcontainers, current Mailpit);
  weigh realism, web UI, shared-first/container-fallback fit, and assertion ergonomics; record the decision
  (migrate / keep / both) with measured numbers on issue #98 (and/or an ADR). The decision rewrites Step 2 below.

### Step 2: Execute decision (CONDITIONAL — `[S]` if Step 1 decides "keep Mailpit")
- [ ] refactor (introduce GreenMail server + reuse listener replacing the Mailpit shared-container path)
- [ ] refactor (replace MailpitTestClient assertions / update EmailStatements to GreenMail API)
- [ ] refactor (update docker/infra-tests.yml + application-test.yml; drop the SMTP greeting-latency workaround)
- [ ] green-acceptance (mail integration suite green deterministically — see regression suite in spec.md)

## Notes
- Spike-first task: Step 2's concrete steps are provisional and will be finalized (or marked `[S]`) by Step 1's decision.
- Step 1 is research/measurement, not a TDD red/green cycle — no test-review/refactor sub-skills apply to the spike itself.
- Migration (Step 2) is verified by the existing full-context mail tests, not new tests (test-infra refactor).
- Discovered during Story 2 (`green-acceptance`, Scenario 1.1).
