import { expect, type Locator, type Page } from '@playwright/test';

// noinspection HardcodedPasswordInspection -- these are data-testid selectors, not credentials
const TEST_ID = {
  passwordInput: 'activation-password-input',
  confirmPasswordInput: 'activation-confirm-password-input',
  mismatchError: 'password-mismatch-error',
} as const;

const MISMATCH_MESSAGE = 'Passwords do not match';

const PASSWORD = 'Str0ng-P@ssw0rd!';
const DIFFERENT_PASSWORD = 'Different-P@ssw0rd!';

export class ActivationMismatchStatements {
  constructor(private readonly page: Page) {}

  async enterMismatchedPasswords(): Promise<void> {
    await this.passwordInput().fill(PASSWORD);
    await this.confirmPasswordInput().fill(DIFFERENT_PASSWORD);
  }

  async assertMismatchErrorIsDisplayed(): Promise<void> {
    await expect(this.mismatchError(), 'password mismatch error message is visible').toBeVisible();
    await expect(this.mismatchError(), 'mismatch error reads "Passwords do not match"').toHaveText(MISMATCH_MESSAGE);
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }

  private confirmPasswordInput(): Locator {
    return this.page.getByTestId(TEST_ID.confirmPasswordInput);
  }

  private mismatchError(): Locator {
    return this.page.getByTestId(TEST_ID.mismatchError);
  }
}
