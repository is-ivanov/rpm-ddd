# Task 215: Show deployed app version in the header -- Progress

Type: feature

## Spec
- [x] spec

## Backend

### Build metadata + version endpoint
- [x] maven (build-info goal + git-commit-id plugin → build-info.properties + git.properties)
- [x] red-acceptance (GET /actuator/info → version/commit/buildTime; 401 when anonymous)
- [x] actuator (green: add starter-actuator; expose info only; info.git/build config)
- [x] security (green: allow-list authenticated GET /actuator/info; deny rest of /actuator/**)
- [x] green-acceptance (remove marker → endpoint passes)

## Frontend

### Header version popover
- [x] red-playwright (click help icon → popover shows version/commit/build time)
- [x] red-frontend (map /actuator/info payload → view model: flatten + 7-char short commit)
- [x] green-frontend
- [x] red-frontend-api (getAppInfo client + payload validation)
- [~] green-frontend-api
- [ ] align-design (HelpCircle icon next to avatar + popover; loading state)
- [ ] green-playwright
- [ ] demo

## Full-Stack Journey
- [ ] fullstack-journey (assess: produce verdict — likely no-impact, non-critical UI affordance)
