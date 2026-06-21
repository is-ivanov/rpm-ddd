# Test Runner Output Format

The `test-runner` agent reports results using this structure:

```
## Test Results

**Module:** {module}
**Test class:** {testClass or "all"}

**Result:** PASSED | FAILED | SKIPPED

**Output:**
```
{test output}
```

**Summary:**
- Tests run: N
- Passed: N
- Failed: N
- Skipped: N

**Failed tests:** (if any)
- TestClass > testMethod() - failure reason
```
