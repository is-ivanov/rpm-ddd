# Task 215: Show deployed app version in the header -- Progress

Type: feature

## Spec
- [x] spec

## Backend

### Build metadata + version endpoint
- [x] maven (build-info goal + git-commit-id plugin → build-info.properties + git.properties)
- [x] red-acceptance (GET /actuator/info → version/commit/buildTime; 401 when anonymous; tag #215)
- [~] actuator (green: add starter-actuator; expose info only; info.git/build config)
- [ ] security (green: allow-list authenticated GET /actuator/info; deny rest of /actuator/**)
- [ ] green-acceptance (remove marker → endpoint passes)

## Frontend

### Header version popover
- [ ] red-playwright (click help icon → popover shows version/commit/build time; tag #215)
- [ ] red-frontend (map /actuator/info payload → view model; [S] if pure pass-through)
- [ ] green-frontend
- [ ] red-frontend-api (getAppInfo client + payload validation; tag #215)
- [ ] green-frontend-api
- [ ] align-design (HelpCircle icon next to avatar + popover; loading state)
- [ ] green-playwright
- [ ] demo

## Full-Stack Journey
- [ ] fullstack-journey (assess: produce verdict — likely no-impact, non-critical UI affordance)
