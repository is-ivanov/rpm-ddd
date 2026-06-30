## align-design (2026-06-23)

**Decision:** Redirect after successful login is an unconditional `router.push('/')` to a fixed path — no return-URL feature.
**Why:** Scenario 4.2 only specified "navigated to the home page"; a `redirectPathAfterLogin()` helper would just return the constant `'/'`, which the trivial-logic gate classifies as never varying by input.
**Where applied:** `LoginPage.vue` submit handler (`useRouter()` + `await router.push('/')` after `login()` resolves).
