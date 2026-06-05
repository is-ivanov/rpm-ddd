import { type Page, type Route } from '@playwright/test';

const LOGIN_URL_PATTERN = '**/api/auth/login';

interface Credential {
  readonly login: string;
  readonly password: string;
}

export class AuthBackendStatements {
  private readonly validCredentials: Credential[] = [];
  private readonly inactiveCredentials: Credential[] = [];

  constructor(private readonly page: Page) {}

  async givenRegisteredUser(login: string, password: string): Promise<void> {
    this.validCredentials.push({ login, password });
    await this.installLoginRoute();
  }

  async givenInactiveUser(login: string, password: string): Promise<void> {
    this.inactiveCredentials.push({ login, password });
    await this.installLoginRoute();
  }

  private async installLoginRoute(): Promise<void> {
    await this.page.route(LOGIN_URL_PATTERN, (route) => this.handleLogin(route));
  }

  private async handleLogin(route: Route): Promise<void> {
    const body = route.request().postDataJSON() as Credential;
    if (this.matchesValidCredential(body)) {
      await this.fulfillSuccess(route);
      return;
    }
    if (this.matchesInactiveCredential(body)) {
      await this.fulfillAccountNotActivated(route);
      return;
    }
    await this.fulfillInvalidCredentials(route);
  }

  private async fulfillSuccess(route: Route): Promise<void> {
    await route.fulfill({ status: 200, contentType: 'application/json', body: '{}' });
  }

  private async fulfillInvalidCredentials(route: Route): Promise<void> {
    await route.fulfill({
      status: 401,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: 'about:blank',
        title: 'Unauthorized',
        status: 401,
        detail: 'Invalid username or password',
        instance: '/api/auth/login',
      }),
    });
  }

  private async fulfillAccountNotActivated(route: Route): Promise<void> {
    await route.fulfill({
      status: 401,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: 'https://www.rpm-ddd.my/problem/authentication-failed',
        title: 'Unauthorized',
        status: 401,
        detail: 'Account not activated',
        instance: '/api/auth/login',
      }),
    });
  }

  private matchesValidCredential(body: Credential): boolean {
    return this.matches(this.validCredentials, body);
  }

  private matchesInactiveCredential(body: Credential): boolean {
    return this.matches(this.inactiveCredentials, body);
  }

  private matches(credentials: Credential[], body: Credential): boolean {
    return credentials.some(
      (credential) => credential.login === body.login && credential.password === body.password,
    );
  }
}
