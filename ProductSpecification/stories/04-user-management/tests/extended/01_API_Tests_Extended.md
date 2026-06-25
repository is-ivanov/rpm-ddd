# API Tests (Extended) — User Management

> These are additional edge case tests. Implement after core tests pass.

## E1. Create with a duplicate email returns a field-level 422

**Given** an existing user with email "alice@example.com"
**When** an admin submits a create-user request with email "alice@example.com" and an otherwise valid body
**Then** the response status is 422
**And** the `fieldErrors` array contains an entry for the `email` field

## E2. Activation updates the audit fields visible in the grid

**Given** a PENDING user created by an admin
**When** that user activates their account and sets a password
**Then** the user's grid row shows status ACTIVE
**And** the row's updatedAt is later than its createdAt
**And** the row's updatedBy resolves to the user themselves (self-service activation)

## E3. List order is stable when two users share the same createdAt

**Given** two users with identical createdAt instants
**When** the admin requests the user list
**Then** the two rows are ordered by userId descending (deterministic tiebreaker)
**And** repeated requests return the same order

## DSL Technical Reference

| Scenario | Method | Path | Auth | Key Assertions |
|---|---|---|---|---|
| E1 | POST | /api/admin/users | JSESSIONID + CSRF | 422 with `fieldErrors[].property == "email"` |
| E2 | (activate then GET) | /api/admin/users | JSESSIONID | row ACTIVE; updatedAt > createdAt; updatedBy == the user |
| E3 | GET | /api/admin/users | JSESSIONID | tie broken by userId DESC; order stable across requests |
