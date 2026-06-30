# Task 250: Fix API clients raw fetch + UsersPage error state — Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: apiFetch in admin-users + current-user API clients; error state in UsersPage
- [x] red-playwright
- [S] red-frontend (trivial: error state is presentational — boolean ref + constant message + try/catch in UsersPage.vue, no .logic.ts function)
- [S] green-frontend (counterpart of skipped red-frontend)
- [x] red-frontend-api
- [x] green-frontend-api
- [~] align-design
- [ ] green-playwright
- [ ] demo
