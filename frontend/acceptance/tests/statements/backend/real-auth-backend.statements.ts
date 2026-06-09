import { type APIRequestContext, type Page, expect, request } from '@playwright/test';

const CSRF_PATH = '/api/auth/csrf';
const LOGIN_PATH = '/api/auth/login';
const REGISTER_USERS_PATH = '/api/admin/users';

const ADMIN_LOGIN = 'admin';
const ADMIN_PASSWORD = 'admin';

const SEED_FIRST_NAME = 'Contract';
const SEED_LAST_NAME = 'User';

const SESSION_COOKIE_NAME = 'JSESSIONID';
const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const COOKIE_PATH = '/';

interface SeededUser {
  readonly login: string;
  readonly email: string;
}

interface BackendCookie {
  readonly name: string;
  readonly value: string;
  readonly path: string;
  readonly httpOnly: boolean;
}

export class RealAuthBackendStatements {
  constructor(
    private readonly page: Page,
    private readonly backendUrl: string,
  ) {}

  async givenActiveUser(user: SeededUser): Promise<void> {
    const api = await this.openApiContext();
    try {
      const adminCsrf = await this.fetchCsrfToken(api);
      await this.loginAsAdmin(api, adminCsrf);
      await this.registerUser(api, adminCsrf, user);
    } finally {
      await api.dispose();
    }
  }

  async assertSessionCookieIsSet(): Promise<void> {
    const cookies = await this.page.context().cookies();
    const session = cookies.find((cookie) => cookie.name === SESSION_COOKIE_NAME);
    expect(session, 'JSESSIONID session cookie is set after a successful login').toBeDefined();
    expect(session!.name, 'session cookie has the exact name JSESSIONID').toBe(SESSION_COOKIE_NAME);
    expect(session!.path, 'JSESSIONID session cookie is scoped to the root path').toBe(COOKIE_PATH);
    expect(session!.httpOnly, 'JSESSIONID session cookie is HttpOnly so script cannot read it').toBe(true);
    expect(session!.value.length, 'JSESSIONID session cookie carries an opaque server-generated id').toBeGreaterThan(0);
  }

  private async openApiContext(): Promise<APIRequestContext> {
    return request.newContext({ baseURL: this.backendUrl });
  }

  private async fetchCsrfToken(api: APIRequestContext): Promise<string> {
    const response = await api.get(CSRF_PATH, { headers: { Accept: 'application/json' } });
    expect(response.status(), 'CSRF token request succeeds against the real backend').toBe(200);
    const csrf = await this.findCookie(api, CSRF_COOKIE_NAME);
    expect(csrf, 'real backend returns an XSRF-TOKEN cookie').toBeDefined();
    expect(csrf!.path, 'XSRF-TOKEN cookie is scoped to the root path').toBe(COOKIE_PATH);
    expect(csrf!.value.length, 'XSRF-TOKEN cookie carries a non-empty token').toBeGreaterThan(0);
    return csrf!.value;
  }

  private async findCookie(api: APIRequestContext, name: string): Promise<BackendCookie | undefined> {
    const { cookies } = await api.storageState();
    return cookies.find((cookie) => cookie.name === name);
  }

  private async loginAsAdmin(api: APIRequestContext, csrfToken: string): Promise<void> {
    const response = await api.post(LOGIN_PATH, {
      headers: this.csrfJsonHeaders(csrfToken),
      data: { login: ADMIN_LOGIN, password: ADMIN_PASSWORD },
    });
    expect(response.status(), 'admin authentication succeeds so user seeding is authorized').toBe(200);
  }

  private async registerUser(api: APIRequestContext, csrfToken: string, user: SeededUser): Promise<void> {
    const response = await api.post(REGISTER_USERS_PATH, {
      headers: this.csrfJsonHeaders(csrfToken),
      data: this.seedPayload(user),
    });
    expect(response.status(), 'real backend creates the seeded user (201 Created)').toBe(201);
  }

  private csrfJsonHeaders(csrfToken: string): Record<string, string> {
    return { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken };
  }

  private seedPayload(user: SeededUser): Record<string, string> {
    return { firstName: SEED_FIRST_NAME, lastName: SEED_LAST_NAME, login: user.login, email: user.email };
  }
}
