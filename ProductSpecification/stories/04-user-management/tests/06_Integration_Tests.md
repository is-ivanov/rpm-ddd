# Integration Tests — User Management

No new integration scenarios. The create-user flow reuses the existing pipeline unchanged: `POST /api/admin/users` publishes `UserRegisteredEvent`, which the activation listener turns into a JWT and an activation email (Story 2 real delivery). Adding the `timeZone` field does not introduce a new external dependency or change the event/email contract.

The activation email delivery is asserted as a side effect of the create happy-path acceptance test (see `01_API_Tests.md` §3.1, which extends the existing registration acceptance test). No external service is added by this story.
