# Scenario 5.7 — Mass assignment is ignored

## red-acceptance (2026-06-15)

**Quirk:** There is no role concept in the domain — `CurrentUserResponse.roles` is hardcoded `List.of()` — so "role remains USER" is not HTTP-observable; `roles=[]` is the strongest available proxy.
**Where:** `CurrentUserResponse` (`/api/auth/me` response body).
**Implication:** Any future role/authorization scenario must introduce a real role concept first; current tests can only assert empty roles.
