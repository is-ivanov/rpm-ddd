# Task 340: mvnw.cmd CRLF line endings -- Progress

Type: bug

## Spec
- [x] spec

## Fix

### Fix: renormalize mvnw.cmd blob to LF
- [S] red/green TDD steps (no production code — repository line-ending metadata only)
- [x] renormalize mvnw.cmd (`git add --renormalize`)
- [ ] re-run checkstyle-updates workflow after merge
- [S] fullstack-journey (no-impact: build tooling only)
