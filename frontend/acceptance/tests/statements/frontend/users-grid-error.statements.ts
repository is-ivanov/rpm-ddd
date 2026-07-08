import { expect, type Locator, type Page } from '@playwright/test';
import { UsersGridLocators } from '../support/users-grid-locators';

const TEST_ID = {
  error: 'users-grid-error',
  errorMessage: 'users-grid-error-message',
  retryButton: 'users-grid-error-retry',
} as const;

const RETRY_BUTTON_TEXT = 'Retry';

export class UsersGridErrorStatements {
  private readonly grid: UsersGridLocators;

  constructor(private readonly page: Page) {
    this.grid = new UsersGridLocators(page);
  }

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
    await expect(this.grid.grid(), 'the users grid is not rendered while the load failed').toHaveCount(0);
  }

  async assertLoadingIndicatorIsGone(): Promise<void> {
    await expect(this.grid.loading(), 'the loading indicator is gone once the request fails').toHaveCount(0);
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
}
