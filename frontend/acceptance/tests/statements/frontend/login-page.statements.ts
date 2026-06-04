import { expect, type Locator, type Page } from '@playwright/test';

// noinspection HardcodedPasswordInspection -- these are data-testid selectors, not credentials
const TEST_ID = {
  loginInput: 'login-input',
  passwordInput: 'password-input',
  passwordToggle: 'password-toggle',
  submitButton: 'submit-button',
} as const;

export class LoginPageStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async navigateToLoginPage(): Promise<void> {
    await this.page.goto(`${this.appUrl}/login`);
  }

  async assertLoginFieldIsVisible(): Promise<void> {
    await expect(this.loginInput(), 'login input field is visible').toBeVisible();
  }

  async assertPasswordFieldIsVisible(): Promise<void> {
    await expect(this.passwordInput(), 'password input field is visible').toBeVisible();
  }

  async assertPasswordFieldMasksText(): Promise<void> {
    await expect(this.passwordInput(), 'password field masks entered text').toHaveAttribute('type', 'password');
  }

  async enterPasswordText(password: string): Promise<void> {
    await this.passwordInput().fill(password);
  }

  async clickPasswordVisibilityToggle(): Promise<void> {
    await this.passwordToggle().click();
  }

  async assertPasswordFieldMasksValue(expectedPassword: string): Promise<void> {
    await expect(this.passwordInput(), 'password field masks entered text').toHaveAttribute('type', 'password');
    await expect(this.passwordInput(), 'password field retains entered text while masked').toHaveValue(
      expectedPassword,
    );
  }

  async assertPasswordFieldRevealsValue(expectedPassword: string): Promise<void> {
    await expect(this.passwordInput(), 'password field reveals entered text in plain form').toHaveAttribute(
      'type',
      'text',
    );
    await expect(this.passwordInput(), 'password field shows the exact entered text when revealed').toHaveValue(
      expectedPassword,
    );
  }

  async assertSubmitButtonIsVisible(): Promise<void> {
    await expect(this.submitButton(), 'submit button is visible').toBeVisible();
    await expect(this.submitButton(), 'submit button has text "Sign In"').toHaveText('Sign In');
  }

  private loginInput(): Locator {
    return this.page.getByTestId(TEST_ID.loginInput);
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }

  private passwordToggle(): Locator {
    return this.page.getByTestId(TEST_ID.passwordToggle);
  }

  private submitButton(): Locator {
    return this.page.getByTestId(TEST_ID.submitButton);
  }
}
