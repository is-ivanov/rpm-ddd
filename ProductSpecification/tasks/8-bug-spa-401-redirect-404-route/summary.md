# Task 8 — Journey Summary

## red-acceptance (2026-06-12)

**Expected:** Guard test "missing asset `/assets/missing.js`" → 404.
**Actual:** 401 Unauthorized.
**Why:** A missing static resource under permitted `/assets/**` raises `NoResourceFoundException`, which triggers an ERROR dispatch to `/error` — and that dispatch is rejected by `anyRequest().denyAll()`, so the browser sees 401, not 404.
**Resolution:** Redesigned the guard to request a real fixture asset (`src/test/resources/static/assets/app.js`) and assert its exact content/Content-Type; raw-401-for-missing-asset is potential improvements-backlog material, out of this task's scope.
