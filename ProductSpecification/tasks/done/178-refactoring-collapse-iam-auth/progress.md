# Task 178: Collapse iam.auth into iam.user (remove auth subpackage) -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Move security adapters auth.infrastructure → user.infrastructure.security
- [x] refactor (move SecurityConfig, RpmUserDetails, RpmUserDetailsService, ProblemDetailAccessDeniedHandler + RpmUserDetailsServiceTest; update package + imports)
- [x] refactor (cleanup: verify imports/static-analysis)

### Step 2: Move auth web auth.infrastructure.web → user.infrastructure.web
- [x] refactor (move AuthResource + LoginRequest/ActivateAccountRequest/CurrentUserResponse/ActivationTokenResponse + AuthResourceTest/ActivateAccountRequestTest; move __files/iam/auth/web → __files/iam/user/web; update package + imports + resource paths)
- [x] refactor (cleanup: verify imports/static-analysis)

### Step 3: Move auth integration tests + fixtures iam.auth → iam.user
- [x] refactor (move integration tests + fixtures package; update package + imports)
- [x] refactor (cleanup: verify imports/static-analysis)

### Step 4: Remove empty auth package and verify
- [x] refactor (delete auth package-info.java files + empty auth dirs)
- [x] green-acceptance (full build: ArchitectureTest + ./mvnw verify green)
