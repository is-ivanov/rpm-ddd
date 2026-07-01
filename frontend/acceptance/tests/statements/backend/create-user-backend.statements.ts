import { type Page, type Route } from '@playwright/test';
import { fulfillCsrfRoute } from '../support/csrf-route';

const ADMIN_USERS_URL_PATTERN = '**/api/admin/users';
const CSRF_URL_PATTERN = '**/api/auth/csrf';

export class CreateUserBackendStatements {
  private releaseInFlightCreate: (() => void) | null = null;

  constructor(private readonly page: Page) {}

  /** Holds POST /api/admin/users in flight until releaseCreateUser(); non-POST requests fall through. */
  async givenCreateUserInFlight(): Promise<void> {
    const held = new Promise<void>((resolve) => {
      this.releaseInFlightCreate = resolve;
    });
    await this.routeCreateUser(async (route) => {
      await held;
      await this.fulfillCreated(route);
    });
  }

  /** Releases the in-flight create so the held 201 response is delivered. */
  releaseCreateUser(): void {
    this.releaseInFlightCreate?.();
  }

  /** Stubs POST /api/admin/users to succeed immediately with 201; non-POST requests fall through. */
  async givenCreateUserSucceeds(): Promise<void> {
    await this.routeCreateUser((route) => this.fulfillCreated(route));
  }

  private async routeCreateUser(onPost: (route: Route) => Promise<void>): Promise<void> {
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
}
