import { expect, type Locator, type Page } from '@playwright/test';

// noinspection HardcodedPasswordInspection -- these are data-testid selectors, not credentials
const TEST_ID = {
  loginInput: 'login-input',
  passwordInput: 'password-input',
  submitButton: 'submit-button',
  errorBanner: 'error-banner',
  errorBannerDismiss: 'error-banner-dismiss',
} as const;

export class LoginErrorDismissStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async givenErrorBannerIsVisible(login: string, wrongPassword: string): Promise<void> {
    await this.page.goto(`${this.appUrl}/login`);
    await this.loginInput().fill(login);
    await this.passwordInput().fill(wrongPassword);
    await this.submitButton().click();
    await expect(this.errorBanner(), 'error banner is visible before dismissal').toBeVisible();
  }

  async clickDismissButton(): Promise<void> {
    await expect(this.dismissButton(), 'banner dismiss button is present and visible').toBeVisible();
    await this.dismissButton().click();
  }

  async assertErrorBannerIsNoLongerVisible(): Promise<void> {
    await expect(this.errorBanner(), 'error banner is no longer visible after dismissal').toHaveCount(0);
  }

  private loginInput(): Locator {
    return this.page.getByTestId(TEST_ID.loginInput);
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }

  private submitButton(): Locator {
    return this.page.getByTestId(TEST_ID.submitButton);
  }

  private errorBanner(): Locator {
    return this.page.getByTestId(TEST_ID.errorBanner);
  }

  private dismissButton(): Locator {
    return this.page.getByTestId(TEST_ID.errorBannerDismiss);
  }
}
