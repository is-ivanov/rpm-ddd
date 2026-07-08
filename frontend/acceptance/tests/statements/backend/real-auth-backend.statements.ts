import { expect, type Cookie, type Page } from '@playwright/test';

const SESSION_COOKIE_NAME = 'JSESSIONID';
const COOKIE_PATH = '/';

export interface CreatedUser {
  readonly firstName: string;
  readonly middleName: string;
  readonly lastName: string;
  readonly login: string;
  readonly email: string;
  readonly password: string;
}

export class RealAuthBackendStatements {
  constructor(private readonly page: Page) {}

  // A unique-per-run identity so the journey's retries (retries: 2) can never
  // collide on "user already exists" against the persistent Postgres. The
  // pre-seeded admin stays fixed; only the CREATED user varies (login + email are
  // the unique parts). Assertions use the returned value, never a literal.
  uniqueUserIdentity(): CreatedUser {
    const suffix = `${Date.now()}_${Math.floor(Math.random() * 1_000_000)}`;
    return {
      firstName: 'Fullstack',
      middleName: 'Journey',
      lastName: 'User',
      login: `fsuser_${suffix}`,
      email: `fsuser_${suffix}@localhost.com`,
      password: 'Fullstack@123', // NOSONAR -- intentional test fixture, not a real credential (S2068)
    };
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

  private async findCookie(name: string): Promise<Cookie | undefined> {
    const cookies = await this.page.context().cookies();
    return cookies.find((cookie) => cookie.name === name);
  }
}
