import { type Page, type Route } from '@playwright/test';

const ME_URL_PATTERN = '**/api/auth/me';

interface AuthenticatedUser {
  readonly firstName: string;
  readonly lastName: string;
}

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
        login: 'ivan.petrov',
        email: 'ivan.petrov@example.com',
        firstName: user.firstName,
        lastName: user.lastName,
        status: 'ACTIVE',
        roles: [],
      }),
    });
  }
}
