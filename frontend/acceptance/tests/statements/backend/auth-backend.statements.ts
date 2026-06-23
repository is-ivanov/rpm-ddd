import { type Page, type Route } from '@playwright/test';
import { LOGIN_FIELD_ERROR_MESSAGE, PASSWORD_FIELD_ERROR_MESSAGE } from '../support/login-validation-messages';
import { fulfillCsrfRoute } from '../support/csrf-route';

const LOGIN_URL_PATTERN = '**/api/auth/login';
const CSRF_URL_PATTERN = '**/api/auth/csrf';

interface Credential {
  readonly login: string;
  readonly password: string;
}

export class AuthBackendStatements {
  private readonly validCredentials: Credential[] = [];
  private readonly inactiveCredentials: Credential[] = [];
  private heldLogin: Promise<void> = Promise.resolve();
  private releaseHeldLogin: () => void = () => {};

  constructor(private readonly page: Page) {}

  async givenRegisteredUser(login: string, password: string): Promise<void> {
    this.validCredentials.push({ login, password });
    await this.installCsrfRoute();
    await this.installLoginRoute();
  }

  async givenInactiveUser(login: string, password: string): Promise<void> {
    this.inactiveCredentials.push({ login, password });
    await this.installCsrfRoute();
    await this.installLoginRoute();
  }

  async givenLoginRequestFails(): Promise<void> {
    await this.installCsrfRoute();
    await this.page.route(LOGIN_URL_PATTERN, (route) => route.abort('failed'));
  }

  async givenSlowLoginRequest(login: string, password: string): Promise<void> {
    this.validCredentials.push({ login, password });
    this.heldLogin = new Promise<void>((resolve) => {
      this.releaseHeldLogin = resolve;
    });
    await this.installCsrfRoute();
    await this.page.route(LOGIN_URL_PATTERN, (route) => this.handleHeldLogin(route));
  }

  async whileSlowLoginIsHeld(action: () => Promise<void>): Promise<void> {
    try {
      await action();
    } finally {
      this.releaseHeldLogin();
    }
  }

  private async handleHeldLogin(route: Route): Promise<void> {
    await this.heldLogin;
    await this.handleLogin(route);
  }

  async givenLoginReturnsFieldValidationErrors(): Promise<void> {
    await this.installCsrfRoute();
    await this.page.route(LOGIN_URL_PATTERN, (route) => this.fulfillFieldValidationErrors(route));
  }

  private async fulfillFieldValidationErrors(route: Route): Promise<void> {
    await route.fulfill({
      status: 422,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: 'https://www.rpm-ddd.my/problem/validation-failed',
        title: 'Unprocessable Content',
        status: 422,
        detail: "Validation failed for object='loginRequest'. Error count: 2.",
        instance: '/api/auth/login',
        fieldErrors: [
          { code: 'NotBlank', property: 'login', message: LOGIN_FIELD_ERROR_MESSAGE, rejectedValue: '', path: 'login' },
          {
            code: 'Size',
            property: 'password',
            message: PASSWORD_FIELD_ERROR_MESSAGE,
            rejectedValue: '',
            path: 'password',
          },
        ],
      }),
    });
  }

  private async installCsrfRoute(): Promise<void> {
    await this.page.route(CSRF_URL_PATTERN, fulfillCsrfRoute);
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
    return credentials.some((credential) => credential.login === body.login && credential.password === body.password);
  }
}
