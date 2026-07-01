import { expect, type Page, type Route } from '@playwright/test';
import { type AdminUser, SEVERAL_ADMIN_USERS } from '../support/admin-users-fixture';
import { NEW_PENDING_USER } from '../support/create-user-fixture';

const ADMIN_USERS_URL_PATTERN = '**/api/admin/users';
const ADMIN_USERS_INSTANCE = '/api/admin/users';
const PROBLEM_TYPE_BASE_URL = 'https://www.rpm-ddd.my/problem/';

interface ProblemDetail {
  status: number;
  typeSuffix: string;
  title: string;
  detail: string;
}

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

  /**
   * Stubs GET /api/admin/users so the initial load returns the seed list, and the
   * post-create refresh (the second GET) returns that list plus the newly-created
   * Pending user prepended (newest-first, matching the backend's createdAt-DESC order).
   */
  async givenListRefreshesWithNewUserAfterCreate(): Promise<void> {
    await this.page.route(ADMIN_USERS_URL_PATTERN, async (route) => {
      if (route.request().method() !== 'GET') {
        await route.fallback();
        return;
      }
      const users =
        this.adminUserListRequestCount === 0 ? SEVERAL_ADMIN_USERS : [NEW_PENDING_USER, ...SEVERAL_ADMIN_USERS];
      await this.fulfillAdminUserList(route, users);
    });
  }

  /** Stubs GET /api/admin/users to return 500 (a recoverable server error). */
  async givenAdminUserListServerError(): Promise<void> {
    await this.page.route(ADMIN_USERS_URL_PATTERN, (route) => this.fulfillServerError(route));
  }

  /** Stubs GET /api/admin/users to return 401 (the session was lost mid-page). */
  async givenAdminUserListUnauthorized(): Promise<void> {
    await this.page.route(ADMIN_USERS_URL_PATTERN, (route) => this.fulfillUnauthorized(route));
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
    await this.fulfillProblemDetail(route, {
      status: 500,
      typeSuffix: 'internal-server-error',
      title: 'Internal Server Error',
      detail: 'An unexpected error occurred while loading users',
    });
  }

  private async fulfillUnauthorized(route: Route): Promise<void> {
    await this.fulfillProblemDetail(route, {
      status: 401,
      typeSuffix: 'unauthorized',
      title: 'Unauthorized',
      detail: 'Full authentication is required to access this resource',
    });
  }

  private async fulfillProblemDetail(route: Route, problem: ProblemDetail): Promise<void> {
    this.adminUserListRequestCount += 1;
    await route.fulfill({
      status: problem.status,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: `${PROBLEM_TYPE_BASE_URL}${problem.typeSuffix}`,
        title: problem.title,
        status: problem.status,
        detail: problem.detail,
        instance: ADMIN_USERS_INSTANCE,
      }),
    });
  }
}
