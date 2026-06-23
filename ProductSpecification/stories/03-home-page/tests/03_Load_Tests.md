# Load Tests — Home Page

No load test scenarios — Home page is frontend-only and adds no new backend load. It issues a single `GET /api/auth/me` per page load (an existing endpoint whose response-time baseline is covered by Story 1's load tests). There is no batch, write, or high-volume path to exercise (see `ExpectedLoad.md`).
