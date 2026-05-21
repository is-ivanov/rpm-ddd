# Story 1: User login — Progress

## Spec
- [x] interview
- [x] story
- [x] mockups
- [x] api-spec
- [x] test-spec

## Backend Scenarios

### Scenario 1.1: Login with PENDING user returns 401
- [x] red-acceptance
- [x] design
- [x] red-usecase
- [x] green-usecase
- [x] adapters-discovery
- [x] green-acceptance

### Scenario 1.2: Login with LOCKED user returns 401
- [x] red-acceptance
- [x] design
- [x] red-usecase
- [x] green-usecase
- [x] adapters-discovery
  - Check 1 (ports): UserRepository — sufficient (findByLogin already implemented from 1.1); PasswordEncoder — sufficient (Spring bean)
  - Check 2 (exceptions): rest — sufficient (UserAuthenticationException already mapped in application.yml from 1.1)
  - Check 3 (response shape): rest — sufficient (error-handling-starter generates ProblemDetail for UserAuthenticationException, same as 1.1)
- [x] green-acceptance

### Scenario 1.3: Login with INACTIVE user returns 401
- [x] red-acceptance
- [S] design (feature already implemented: UserStatus.INACTIVE handled by existing authentication code)
- [S] red-usecase (feature already implemented: UserStatus.authenticationErrorMessage() covers INACTIVE)
- [S] green-usecase (feature already implemented)
- [S] adapters-discovery (feature already implemented: same code path as PENDING/LOCKED)
- [x] green-acceptance

### Scenario 2.1: Valid activation token returns user info
- [x] red-acceptance
- [x] design
- [x] red-usecase
- [x] green-usecase
- [x] adapters-discovery
  - Check 1 (ports): UserRepository.findById — sufficient (simple Spring Data derived query); JwtActivationTokenGenerator — domain service, not a port
  - Check 2 (exceptions): rest — sufficient (UserNotFoundException not triggered in happy path; will be needed for Scenarios 2.2/2.3)
  - Check 3 (response shape): rest — MISSING endpoint GET /api/auth/activate?token=... with response DTO {login, email}
- [x] red-adapter rest
- [x] green-adapter rest
- [~] green-acceptance

### Scenario 2.2: Expired activation token returns error
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 2.3: Invalid activation token returns error
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 3.1: Activate with password violating policy returns validation errors
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 3.2: Activate with expired token returns error
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 4.1: Activate with valid token and password succeeds
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.1: Authenticated user retrieves own info
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.2: Unauthenticated request to /me returns 401
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 6.1: Logout invalidates session
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [ ] adapters-discovery
- [ ] green-acceptance
