import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  grid: 'users-grid',
  loading: 'users-grid-loading',
  error: 'users-grid-error',
  errorMessage: 'users-grid-error-message',
  retryButton: 'users-grid-error-retry',
} as const;

const RETRY_BUTTON_TEXT = 'Retry';

export class UsersGridErrorStatements {
  constructor(private readonly page: Page) {}

  async assertErrorStateIsVisible(): Promise<void> {
    await expect(this.error(), 'users grid error state is visible after the list request fails').toBeVisible();
  }

  async assertErrorMessageIsVisible(): Promise<void> {
    await expect(this.errorMessage(), 'an error message explaining the load failure is visible').toBeVisible();
    await expect(this.errorMessage(), 'the error message is not empty').not.toBeEmpty();
  }

  async assertRetryButtonIsVisible(): Promise<void> {
    await expect(this.retryButton(), 'a retry button is visible in the error state').toBeVisible();
    await expect(this.retryButton(), 'retry button text is exactly "Retry"').toHaveText(RETRY_BUTTON_TEXT);
  }

  async assertGridIsNotShown(): Promise<void> {
    await expect(this.grid(), 'the users grid is not rendered while the load failed').toHaveCount(0);
  }

  async assertLoadingIndicatorIsGone(): Promise<void> {
    await expect(this.loading(), 'the loading indicator is gone once the request fails').toHaveCount(0);
  }

  async assertLoadFailureStateIsShown(): Promise<void> {
    await this.assertErrorStateIsVisible();
    await this.assertErrorMessageIsVisible();
    await this.assertRetryButtonIsVisible();
    await this.assertLoadingIndicatorIsGone();
    await this.assertGridIsNotShown();
  }

  private error(): Locator {
    return this.page.getByTestId(TEST_ID.error);
  }

  private errorMessage(): Locator {
    return this.page.getByTestId(TEST_ID.errorMessage);
  }

  private retryButton(): Locator {
    return this.page.getByTestId(TEST_ID.retryButton);
  }

  private grid(): Locator {
    return this.page.getByTestId(TEST_ID.grid);
  }

  private loading(): Locator {
    return this.page.getByTestId(TEST_ID.loading);
  }
}
