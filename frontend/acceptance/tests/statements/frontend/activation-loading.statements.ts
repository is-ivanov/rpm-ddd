import { expect, type Locator, type Page } from '@playwright/test';

// noinspection HardcodedPasswordInspection -- these are data-testid selectors, not credentials
const TEST_ID = {
  passwordInput: 'activation-password-input',
  confirmPasswordInput: 'activation-confirm-password-input',
  activateButton: 'activate-button',
  activateLoading: 'activate-loading',
} as const;

const VALID_PASSWORD = 'Str0ng-P@ssw0rd!';

export class ActivationLoadingStatements {
  constructor(private readonly page: Page) {}

  async submitValidMatchingPassword(): Promise<void> {
    await this.passwordInput().fill(VALID_PASSWORD);
    await this.confirmPasswordInput().fill(VALID_PASSWORD);
    await this.activateButton().click();
  }

  async assertLoadingStateIsActive(): Promise<void> {
    await this.assertActivateButtonShowsLoadingIndicator();
    await this.assertPasswordFieldIsDisabled();
    await this.assertConfirmPasswordFieldIsDisabled();
  }

  private async assertActivateButtonShowsLoadingIndicator(): Promise<void> {
    await expect(
      this.activateLoadingIndicator(),
      'activate button shows a loading indicator during submission',
    ).toBeVisible();
  }

  private async assertPasswordFieldIsDisabled(): Promise<void> {
    await expect(this.passwordInput(), 'password field is disabled during submission').toBeDisabled();
  }

  private async assertConfirmPasswordFieldIsDisabled(): Promise<void> {
    await expect(this.confirmPasswordInput(), 'confirm password field is disabled during submission').toBeDisabled();
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }

  private confirmPasswordInput(): Locator {
    return this.page.getByTestId(TEST_ID.confirmPasswordInput);
  }

  private activateButton(): Locator {
    return this.page.getByTestId(TEST_ID.activateButton);
  }

  private activateLoadingIndicator(): Locator {
    return this.page.getByTestId(TEST_ID.activateLoading);
  }
}
