import { type Page, type Route } from '@playwright/test';

const ME_URL_PATTERN = '**/api/auth/me';

interface AuthenticatedUser {
  readonly firstName: string;
  readonly lastName: string;
  readonly email?: string;
}

const CURRENT_USER_LOGIN = 'ivan.petrov';
const EMAIL_DOMAIN = 'rpm.local';
// Used only when a caller omits the email (the field is not asserted by that scenario);
// derived from the login so it stays a real, suite-consistent address, not a mockup placeholder.
const DEFAULT_EMAIL = `${CURRENT_USER_LOGIN}@${EMAIL_DOMAIN}`;

export class CurrentUserBackendStatements {
  constructor(private readonly page: Page) {}

  async givenUnauthenticated(): Promise<void> {
    await this.page.route(ME_URL_PATTERN, (route) => this.fulfillUnauthenticatedCurrentUser(route));
  }

  async givenAuthenticatedUser(user: AuthenticatedUser): Promise<void> {
    await this.page.route(ME_URL_PATTERN, (route) => this.fulfillAuthenticatedCurrentUser(route, user));
  }

  private async fulfillUnauthenticatedCurrentUser(route: Route): Promise<void> {
    await route.fulfill({
      status: 401,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: 'https://www.rpm-ddd.my/problem/unauthorized',
        title: 'Unauthorized',
        status: 401,
        detail: 'Full authentication is required to access this resource',
        instance: '/api/auth/me',
      }),
    });
  }

  private async fulfillAuthenticatedCurrentUser(route: Route, user: AuthenticatedUser): Promise<void> {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        userId: '11111111-1111-1111-1111-111111111111',
        login: CURRENT_USER_LOGIN,
        email: user.email ?? DEFAULT_EMAIL,
        firstName: user.firstName,
        lastName: user.lastName,
        status: 'ACTIVE',
        roles: [],
      }),
    });
  }
}
