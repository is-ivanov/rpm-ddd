import { type Page, type Route } from '@playwright/test';
import { type AdminUser, SEVERAL_ADMIN_USERS } from '../support/admin-users-fixture';

const ADMIN_USERS_URL_PATTERN = '**/api/admin/users';

export class AdminUsersBackendStatements {
  constructor(private readonly page: Page) {}

  async givenSeveralUsers(): Promise<void> {
    await this.givenAdminUserListReturns(SEVERAL_ADMIN_USERS);
  }

  async givenAdminUserListReturns(users: readonly AdminUser[]): Promise<void> {
    await this.page.route(ADMIN_USERS_URL_PATTERN, (route) => this.fulfillAdminUserList(route, users));
  }

  private async fulfillAdminUserList(route: Route, users: readonly AdminUser[]): Promise<void> {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(users),
    });
  }
}
