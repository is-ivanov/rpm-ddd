# Task 251: Reactive 401 redirect to /login — Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: watch isAuthenticated → false → router.push(/login)
- [x] red-playwright
- [x] red-frontend
- [x] green-frontend
- [S] red-frontend-api (no API-client change: 401→reset lives in apiFetch/fetch.api.ts from #250; no new endpoint)
- [S] green-frontend-api (no API-client change: see red-frontend-api)
- [~] align-design
- [ ] green-playwright
- [ ] demo
