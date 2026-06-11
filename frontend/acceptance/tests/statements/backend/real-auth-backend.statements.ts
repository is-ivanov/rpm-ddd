import { expect, type Cookie, type Page } from '@playwright/test';

const CSRF_PATH = '/api/auth/csrf';
const REGISTER_USERS_PATH = '/api/admin/users';

const SEED_FIRST_NAME = 'Contract';
const SEED_LAST_NAME = 'User';

const SESSION_COOKIE_NAME = 'JSESSIONID';
const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const COOKIE_PATH = '/';

// POST /api/admin/users returns 201 with Location: /api/admin/users/{id} and an
// empty body (ResponseEntity<Void>). The {id} is an opaque server-generated UUID
// with no retrieval API at this point in the journey, so the path PREFIX is the
// deterministic part we pin; the created identity itself is round-tripped later
// (activation email to user.email, final UI login as user.login).
const CREATED_USER_LOCATION_PATTERN = /\/api\/admin\/users\/[^/\s]+$/;

export interface CreatedUser {
  readonly login: string;
  readonly email: string;
  readonly password: string;
}

export class RealAuthBackendStatements {
  constructor(private readonly page: Page) {}

  // A unique-per-run identity so the journey's retries (retries: 2) can never
  // collide on "user already exists" against the persistent Postgres. The
  // pre-seeded admin stays fixed; only the CREATED user varies. Assertions use
  // the returned value, never a literal.
  uniqueUserIdentity(): CreatedUser {
    const suffix = `${Date.now()}_${Math.floor(Math.random() * 1_000_000)}`;
    return {
      login: `fsuser_${suffix}`,
      email: `fsuser_${suffix}@localhost.com`,
      password: 'Fullstack@123', // NOSONAR -- intentional test fixture, not a real credential (S2068)
    };
  }

  // Admin creates the new user via the real REST API, reusing the browser's
  // already-authenticated admin session (JSESSIONID set by the UI login) plus a
  // freshly primed CSRF token. The created user is PENDING and is activated via
  // the real email flow later in the journey.
  // NOTE: This step uses the API because no admin-create-user UI exists yet.
  // When that UI ships, migrate this to UI actions (see the ADR edge-case table).
  async createUserAsAdmin(user: CreatedUser): Promise<void> {
    const csrfToken = await this.primeCsrfToken();
    const response = await this.page.request.post(REGISTER_USERS_PATH, {
      headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken },
      data: { firstName: SEED_FIRST_NAME, lastName: SEED_LAST_NAME, login: user.login, email: user.email },
    });
    expect(response.status(), 'admin creates the new user via the real backend (201 Created)').toBe(201);
    expect(
      response.headers()['location'],
      'create-user 201 carries a Location pointing at the new user resource',
    ).toMatch(CREATED_USER_LOCATION_PATTERN);
  }

  async assertSessionCookieIsSet(): Promise<void> {
    // The browser login is an async fetch with no post-success UI signal to await,
    // so poll the cookie jar (never sleep) until the backend's Set-Cookie lands.
    await expect
      .poll(async () => (await this.sessionCookie())?.value.length ?? 0, {
        message: 'JSESSIONID session cookie is set after a successful login',
        timeout: 10_000,
      })
      .toBeGreaterThan(0);
    const session = await this.sessionCookie();
    expect(session, 'JSESSIONID session cookie is set after a successful login').toBeDefined();
    expect(session!.name, 'session cookie has the exact name JSESSIONID').toBe(SESSION_COOKIE_NAME);
    expect(session!.path, 'JSESSIONID session cookie is scoped to the root path').toBe(COOKIE_PATH);
    expect(session!.httpOnly, 'JSESSIONID session cookie is HttpOnly so script cannot read it').toBe(true);
    expect(session!.value.length, 'JSESSIONID session cookie carries an opaque server-generated id').toBeGreaterThan(0);
  }

  private async sessionCookie(): Promise<Cookie | undefined> {
    return this.findCookie(SESSION_COOKIE_NAME);
  }

  private async primeCsrfToken(): Promise<string> {
    const response = await this.page.request.get(CSRF_PATH, { headers: { Accept: 'application/json' } });
    expect(response.status(), 'CSRF token request succeeds against the real backend').toBe(200);
    const csrf = await this.findCookie(CSRF_COOKIE_NAME);
    expect(csrf, 'real backend returns an XSRF-TOKEN cookie').toBeDefined();
    expect(csrf!.path, 'XSRF-TOKEN cookie is scoped to the root path').toBe(COOKIE_PATH);
    expect(csrf!.value.length, 'XSRF-TOKEN cookie carries a non-empty token').toBeGreaterThan(0);
    return csrf!.value;
  }

  private async findCookie(name: string): Promise<Cookie | undefined> {
    const cookies = await this.page.context().cookies();
    return cookies.find((cookie) => cookie.name === name);
  }
}
