# Task 228: Validation test helper -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Create the shared ConstraintViolation argumentSet test helper
- [ ] refactor (build the helper in by.iivanov.rpm.testing: concise builders for invalid argumentSet cases — NotBlank/Size/Email/composite + combined-violation cases; keep the valid-model + @ParameterizedTest shape reusable)
- [ ] refactor (cleanup)

### Step 2: Refactor RegisterUserRequestTest onto the helper
- [ ] refactor (replace local blankCase/sizeCase/notBlank/size/emailFormat with the shared helper; keep literal boundary limits)
- [ ] refactor (cleanup)

### Step 3: Refactor ActivateAccountRequestTest onto the helper + literal boundaries
- [ ] refactor (adopt the shared helper; replace PasswordPolicy.MIN_LENGTH/MAX_LENGTH-derived inputs/messages with pinned literals, constant named only in a comment)
- [ ] refactor (cleanup)
