# Task 178: Collapse iam.auth into iam.user (remove auth subpackage) -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Move security adapters auth.infrastructure → user.infrastructure.security
- [ ] refactor (move SecurityConfig, RpmUserDetails, RpmUserDetailsService, ProblemDetailAccessDeniedHandler + RpmUserDetailsServiceTest; update package + imports)
- [ ] refactor (cleanup: verify imports/static-analysis)

### Step 2: Move auth web auth.infrastructure.web → user.infrastructure.web
- [ ] refactor (move AuthResource + LoginRequest/ActivateAccountRequest/CurrentUserResponse/ActivationTokenResponse + AuthResourceTest/ActivateAccountRequestTest; move __files/iam/auth/web → __files/iam/user/web; update package + imports + resource paths)
- [ ] refactor (cleanup: verify imports/static-analysis)

### Step 3: Move auth integration tests + fixtures iam.auth → iam.user
- [ ] refactor (move integration tests + fixtures package; update package + imports)
- [ ] refactor (cleanup: verify imports/static-analysis)

### Step 4: Remove empty auth package and verify
- [ ] refactor (delete auth package-info.java files + empty auth dirs)
- [ ] green-acceptance (full build: ArchitectureTest + ./mvnw verify green)
