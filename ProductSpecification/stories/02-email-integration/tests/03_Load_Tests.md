# Load Tests — Email Integration

No load test scenarios — email volume is one message per registration, and registrations are rare relative to the platform's IoT reading load (see `ExpectedLoad.txt`). Sending is asynchronous (Modulith listener) and never blocks the registration HTTP response, and the 5-second resubmit scheduler scans only incomplete publications (normally empty). There are no throughput or rate-limit concerns to verify under load.
