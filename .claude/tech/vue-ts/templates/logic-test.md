# Frontend Logic Test Template

## Test File

Location: `frontend/src/features/{feature}/__tests__/{feature}.logic.test.ts`

```typescript
import { describe, it, expect } from 'vitest'
import { validateEmail, validatePassword, isFormValid, buildRegistrationRequest } from '../logic/registration.logic'

describe('Registration Logic', () => {
  describe('validateEmail', () => {
    it('should return valid for correct email format', () => {
      const result = validateEmail('user@example.com')

      expect(result.valid).toBe(true)
      expect(result.error).toBeUndefined()
    })

    it('should return error for empty email', () => {
      const result = validateEmail('')

      expect(result.valid).toBe(false)
      expect(result.error).toBe('Email is required')
    })
  })
})
```

## Stub

Create minimal stub in `logic/{feature}.logic.ts`:

```typescript
import type { ValidationResult } from './types'

export function validateEmail(email: string): ValidationResult {
  throw new Error('Not implemented')
}
```

## Types

Create `logic/types.ts` if not present:

```typescript
export interface ValidationResult {
  valid: boolean
  error?: string
}
```

## Expected Failure Patterns

| Stub | Expected Failure |
|------|-----------------|
| `throw new Error('Not implemented')` | Error: Not implemented |
| `return undefined` | expect(undefined).toBe(true) fails |
| No function exported | Import error |

## .fails Convention (RED-phase marker)

After verified failure, mark the test `it.fails` (the RED-phase marker — see
`.claude/tech/vue-ts/tdd.md` → "RED-Phase Marker") with a comment naming the
predicted failure. The test still **runs** every build: it stays green while it
fails, and once GREEN makes it pass, the build fails (`Expect test to fail`),
forcing you to drop `.fails`.

```typescript
// RED — validateEmail not implemented (throws 'Not implemented')
it.fails('should return valid for correct email format', () => {
  // ... test unchanged ...
})
```

**Pin the RED reason via the assertion.** `it.fails` has no error-type pin (no
`withExceptions` analog): any failure in the body counts as "expected fail". Keep a
specific `expect(...)` that fails for the *predicted* reason, so an incidental
failure (typo, import error) is not silently absorbed.

At GREEN, remove only the `.fails` modifier — back to `it('should ...', () => {`.

## Test Verification

```
Skill tool: skill="test-frontend", args="{feature}.logic"
```
