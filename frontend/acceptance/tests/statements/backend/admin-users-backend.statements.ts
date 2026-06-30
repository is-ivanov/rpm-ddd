import { expect, type Page, type Route } from '@playwright/test';
import { type AdminUser, SEVERAL_ADMIN_USERS } from '../support/admin-users-fixture';

const ADMIN_USERS_URL_PATTERN = '**/api/admin/users';

export class AdminUsersBackendStatements {
  private releaseInFlightList: (() => void) | null = null;
  private adminUserListRequestCount = 0;

  constructor(private readonly page: Page) {}

  /** Asserts the admin user list was fetched exactly once (the initial load, no filter refetch). */
  assertAdminUserListRequestedOnce(): void {
    expect(
      this.adminUserListRequestCount,
      'only the initial load fetched /api/admin/users; the client-side filter fired no extra request',
    ).toBe(1);
  }

  async givenSeveralUsers(): Promise<void> {
    await this.givenAdminUserListReturns(SEVERAL_ADMIN_USERS);
  }

  /** Stubs GET /api/admin/users to return 500 (a recoverable server error). */
  async givenAdminUserListServerError(): Promise<void> {
    await this.page.route(ADMIN_USERS_URL_PATTERN, (route) => this.fulfillServerError(route));
  }

  async givenAdminUserListReturns(users: readonly AdminUser[]): Promise<void> {
    await this.page.route(ADMIN_USERS_URL_PATTERN, (route) => this.fulfillAdminUserList(route, users));
  }

  /** Holds the GET /api/admin/users response in flight until releaseAdminUserList() is called. */
  async givenAdminUserListInFlight(): Promise<void> {
    const held = new Promise<void>((resolve) => {
      this.releaseInFlightList = resolve;
    });
    await this.page.route(ADMIN_USERS_URL_PATTERN, async (route) => {
      await held;
      await this.fulfillAdminUserList(route, SEVERAL_ADMIN_USERS);
    });
  }

  /** Releases the in-flight admin user list so the held response is delivered. */
  releaseAdminUserList(): void {
    this.releaseInFlightList?.();
  }

  private async fulfillAdminUserList(route: Route, users: readonly AdminUser[]): Promise<void> {
    this.adminUserListRequestCount += 1;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(users),
    });
  }

  private async fulfillServerError(route: Route): Promise<void> {
    this.adminUserListRequestCount += 1;
    await route.fulfill({
      status: 500,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: 'https://www.rpm-ddd.my/problem/internal-server-error',
        title: 'Internal Server Error',
        status: 500,
        detail: 'An unexpected error occurred while loading users',
        instance: '/api/admin/users',
      }),
    });
  }
}
