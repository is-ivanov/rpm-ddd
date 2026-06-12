import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  notFoundPage: 'not-found-page',
  notFoundTitle: 'not-found-title',
  notFoundMessage: 'not-found-message',
  backHomeLink: 'back-home-link',
} as const;

export class NotFoundPageStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async navigateToUnknownRoute(): Promise<void> {
    await this.page.goto(`${this.appUrl}/this-route-does-not-exist`);
  }

  async assertNotFoundViewIsVisible(): Promise<void> {
    await expect(this.notFoundView(), 'NotFound view container is visible').toBeVisible();
    await expect(this.notFoundTitle(), '404 title is visible').toBeVisible();
    await expect(this.notFoundTitle(), '404 title shows "404"').toHaveText('404');
  }

  async assertNotFoundMessageIsVisible(): Promise<void> {
    await expect(this.notFoundMessage(), 'friendly 404 message is visible').toBeVisible();
    await expect(this.notFoundMessage(), 'friendly 404 message shows the exact text').toHaveText('Page not found');
  }

  async assertBackToHomeLinkIsVisible(): Promise<void> {
    await expect(this.backHomeLink(), 'back-to-home link is visible').toBeVisible();
    await expect(this.backHomeLink(), 'back-to-home link shows the exact text').toHaveText('Back to home');
  }

  async clickBackToHomeLink(): Promise<void> {
    await this.backHomeLink().click();
  }

  private notFoundView(): Locator {
    return this.page.getByTestId(TEST_ID.notFoundPage);
  }

  private notFoundTitle(): Locator {
    return this.page.getByTestId(TEST_ID.notFoundTitle);
  }

  private notFoundMessage(): Locator {
    return this.page.getByTestId(TEST_ID.notFoundMessage);
  }

  private backHomeLink(): Locator {
    return this.page.getByTestId(TEST_ID.backHomeLink);
  }
}
