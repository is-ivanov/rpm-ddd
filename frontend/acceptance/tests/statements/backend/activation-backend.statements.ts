import { type Page, type Route } from '@playwright/test';

const ACTIVATE_URL_PATTERN = '**/api/auth/activate*';
const CSRF_URL_PATTERN = '**/api/auth/csrf';

const XSRF_TOKEN = 'test-xsrf-token';

interface Account {
  readonly login: string;
  readonly email: string;
}

export class ActivationBackendStatements {
  constructor(private readonly page: Page) {}

  async givenPendingAccountForToken(account: Account): Promise<void> {
    await this.installCsrfRoute();
    await this.page.route(ACTIVATE_URL_PATTERN, (route) => this.handleActivate(route, account));
  }

  private async installCsrfRoute(): Promise<void> {
    await this.page.route(CSRF_URL_PATTERN, (route) => this.handleCsrf(route));
  }

  private async handleCsrf(route: Route): Promise<void> {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      headers: { 'Set-Cookie': `XSRF-TOKEN=${XSRF_TOKEN}; Path=/` },
      body: '{}',
    });
  }

  private async handleActivate(route: Route, account: Account): Promise<void> {
    if (route.request().method() === 'POST') {
      await this.fulfillActivationSuccess(route);
      return;
    }
    await this.fulfillTokenValidation(route, account);
  }

  private async fulfillTokenValidation(route: Route, account: Account): Promise<void> {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ login: account.login, email: account.email }),
    });
  }

  private async fulfillActivationSuccess(route: Route): Promise<void> {
    await route.fulfill({ status: 200, contentType: 'application/json', body: '{}' });
  }
}
