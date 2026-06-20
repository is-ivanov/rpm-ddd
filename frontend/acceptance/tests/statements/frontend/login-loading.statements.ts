import { expect, type Locator, type Page } from '@playwright/test';

// noinspection HardcodedPasswordInspection -- these are data-testid selectors, not credentials
const TEST_ID = {
  loginInput: 'login-input',
  passwordInput: 'password-input',
  submitButton: 'submit-button',
  submitLoading: 'submit-loading',
} as const;

export class LoginLoadingStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async navigateToLoginPage(): Promise<void> {
    await this.page.goto(`${this.appUrl}/login`);
  }

  async submitValidCredentials(login: string, password: string): Promise<void> {
    await this.loginInput().fill(login);
    await this.passwordInput().fill(password);
    await this.submitButton().click();
  }

  async assertLoadingStateIsActive(): Promise<void> {
    await this.assertSubmitButtonShowsLoadingIndicator();
    await this.assertLoginFieldIsDisabled();
    await this.assertPasswordFieldIsDisabled();
  }

  private async assertSubmitButtonShowsLoadingIndicator(): Promise<void> {
    await expect(
      this.submitLoadingIndicator(),
      'submit button shows a loading indicator during submission',
    ).toBeVisible();
  }

  private async assertLoginFieldIsDisabled(): Promise<void> {
    await expect(this.loginInput(), 'login field is disabled during submission').toBeDisabled();
  }

  private async assertPasswordFieldIsDisabled(): Promise<void> {
    await expect(this.passwordInput(), 'password field is disabled during submission').toBeDisabled();
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

  private submitLoadingIndicator(): Locator {
    return this.page.getByTestId(TEST_ID.submitLoading);
  }
}
