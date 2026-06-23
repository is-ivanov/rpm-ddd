import { type Page, type Route } from '@playwright/test';

const ACTIVATE_URL_PATTERN = '**/api/auth/activate*';
const CSRF_URL_PATTERN = '**/api/auth/csrf';

const XSRF_TOKEN = 'test-xsrf-token';

interface Account {
  readonly login: string;
  readonly email: string;
}

interface ProblemDetail {
  readonly type: string;
  readonly title: string;
  readonly status: number;
  readonly detail: string;
  readonly instance?: string;
}

export class ActivationBackendStatements {
  private heldActivation: Promise<void> = Promise.resolve();
  private releaseHeldActivation: () => void = () => {};

  constructor(private readonly page: Page) {}

  async givenPendingAccountForToken(account: Account): Promise<void> {
    await this.installActivationRoutes((route) =>
      this.handleValidToken(route, account, (post) => this.fulfillActivationSuccess(post)),
    );
  }

  async givenSlowActivationRequest(account: Account): Promise<void> {
    this.heldActivation = new Promise<void>((resolve) => {
      this.releaseHeldActivation = resolve;
    });
    await this.installActivationRoutes((route) =>
      this.handleValidToken(route, account, (post) => this.handleHeldActivation(post)),
    );
  }

  async whileSlowActivationIsHeld(action: () => Promise<void>): Promise<void> {
    try {
      await action();
    } finally {
      this.releaseHeldActivation();
    }
  }

  private async handleHeldActivation(route: Route): Promise<void> {
    await this.heldActivation;
    await this.fulfillActivationSuccess(route);
  }

  async givenExpiredToken(): Promise<void> {
    await this.installActivationRoutes((route) => this.handleExpiredToken(route));
  }

  async givenValidTokenButActivationRejectsWeakPassword(account: Account): Promise<void> {
    await this.installActivationRoutes((route) =>
      this.handleValidToken(route, account, (post) => this.fulfillWeakPasswordRejection(post)),
    );
  }

  private async installActivationRoutes(handler: (route: Route) => Promise<void>): Promise<void> {
    await this.installCsrfRoute();
    await this.page.route(ACTIVATE_URL_PATTERN, handler);
  }

  private async handleExpiredToken(route: Route): Promise<void> {
    await this.fulfillProblem(route, {
      type: 'about:blank',
      title: 'Unprocessable Entity',
      status: 422,
      detail: 'The activation link is invalid or has expired.',
    });
  }

  private async fulfillProblem(route: Route, problem: ProblemDetail): Promise<void> {
    await route.fulfill({
      status: problem.status,
      contentType: 'application/problem+json',
      body: JSON.stringify(problem),
    });
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

  private async handleValidToken(
    route: Route,
    account: Account,
    fulfillPost: (route: Route) => Promise<void>,
  ): Promise<void> {
    if (route.request().method() === 'POST') {
      await fulfillPost(route);
      return;
    }
    await this.fulfillTokenValidation(route, account);
  }

  private async fulfillWeakPasswordRejection(route: Route): Promise<void> {
    await this.fulfillProblem(route, {
      type: 'about:blank',
      title: 'Unprocessable Entity',
      status: 422,
      detail: 'Password does not meet the complexity requirements.',
      instance: '/api/auth/activate',
    });
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
