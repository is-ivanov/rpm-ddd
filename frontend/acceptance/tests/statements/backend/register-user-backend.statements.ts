import { type Page, type Route } from '@playwright/test';
import { fulfillCsrfRoute } from '../support/csrf-route';
import { DUPLICATE_LOGIN_ERROR_MESSAGE, NEW_USER_INPUT } from '../support/register-user-fixture';

const ADMIN_USERS_URL_PATTERN = '**/api/admin/users';
const CSRF_URL_PATTERN = '**/api/auth/csrf';
const PROBLEM_TYPE_BASE_URL = 'https://www.rpm-ddd.my/problem/';

export class RegisterUserBackendStatements {
  private releaseInFlightCreate: (() => void) | null = null;

  constructor(private readonly page: Page) {}

  /** Holds POST /api/admin/users in flight until releaseRegisterUser(); non-POST requests fall through. */
  async givenRegisterUserInFlight(): Promise<void> {
    const held = new Promise<void>((resolve) => {
      this.releaseInFlightCreate = resolve;
    });
    await this.routeRegisterUser(async (route) => {
      await held;
      await this.fulfillCreated(route);
    });
  }

  /** Releases the in-flight create so the held 201 response is delivered. */
  releaseRegisterUser(): void {
    this.releaseInFlightCreate?.();
  }

  /** Stubs POST /api/admin/users to succeed immediately with 201; non-POST requests fall through. */
  async givenRegisterUserSucceeds(): Promise<void> {
    await this.routeRegisterUser((route) => this.fulfillCreated(route));
  }

  /**
   * Stubs POST /api/admin/users to reject with a 422 problem+json duplicate-login field error
   * (the RFC-9457 shape the real backend emits); non-POST requests fall through.
   */
  async givenRegisterUserRejectsDuplicateLogin(): Promise<void> {
    await this.routeRegisterUser((route) => this.fulfillDuplicateLogin(route));
  }

  private async routeRegisterUser(onPost: (route: Route) => Promise<void>): Promise<void> {
    await this.page.route(CSRF_URL_PATTERN, fulfillCsrfRoute);
    await this.page.route(ADMIN_USERS_URL_PATTERN, async (route) => {
      if (route.request().method() !== 'POST') {
        await route.fallback();
        return;
      }
      await onPost(route);
    });
  }

  private async fulfillCreated(route: Route): Promise<void> {
    await route.fulfill({
      status: 201,
      contentType: 'application/json',
      body: '{}',
    });
  }

  private async fulfillDuplicateLogin(route: Route): Promise<void> {
    await route.fulfill({
      status: 422,
      contentType: 'application/problem+json',
      body: JSON.stringify({
        type: `${PROBLEM_TYPE_BASE_URL}validation-failed`,
        title: 'Validation failed',
        status: 422,
        detail: "Validation failed for object='registerUserRequest'. Error count: 1",
        fieldErrors: [
          {
            code: 'ALREADY_EXISTS',
            property: 'login',
            message: DUPLICATE_LOGIN_ERROR_MESSAGE,
            rejectedValue: NEW_USER_INPUT.login,
            path: 'login',
          },
        ],
      }),
    });
  }
}
