import { expect, type Locator, type Page } from '@playwright/test';
import { LOGIN_FIELD_ERROR_MESSAGE, PASSWORD_FIELD_ERROR_MESSAGE } from '../support/login-validation-messages';

// noinspection HardcodedPasswordInspection -- these are data-testid selectors, not credentials
const TEST_ID = {
  loginInput: 'login-input',
  passwordInput: 'password-input',
  passwordToggle: 'password-toggle',
  submitButton: 'submit-button',
  errorBanner: 'error-banner',
  activationLink: 'activation-link',
  loginError: 'login-error',
  passwordError: 'password-error',
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

  async enterLoginText(login: string): Promise<void> {
    await this.loginInput().fill(login);
  }

  async enterPasswordText(password: string): Promise<void> {
    await this.passwordInput().fill(password);
  }

  async clickSubmitButton(): Promise<void> {
    await this.submitButton().click();
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

  async assertSubmitButtonIsDisabled(): Promise<void> {
    await expect(this.submitButton(), 'submit button is disabled until both fields are filled').toBeDisabled();
  }

  async assertSubmitButtonIsEnabled(): Promise<void> {
    await expect(this.submitButton(), 'submit button is enabled once both fields are filled').toBeEnabled();
  }

  async assertErrorBannerShowsInvalidCredentials(): Promise<void> {
    await expect(this.errorBanner(), 'error banner is visible').toBeVisible();
    await expect(this.errorBanner(), 'error banner shows invalid credentials message').toHaveText(
      'Invalid username or password',
    );
  }

  async assertErrorBannerShowsGenericError(): Promise<void> {
    await expect(this.errorBanner(), 'error banner is visible').toBeVisible();
    await expect(this.errorBanner(), 'error banner shows generic error message').toHaveText(
      'Something went wrong. Please try again.',
    );
  }

  async assertErrorBannerShowsActivationRequired(): Promise<void> {
    await expect(this.errorBanner(), 'error banner is visible').toBeVisible();
    await expect(this.errorBanner(), 'error banner indicates the account requires activation').toContainText(
      'Account not activated',
    );
  }

  async assertErrorBannerContainsActivationLink(): Promise<void> {
    await expect(this.activationLink(), 'activation link is visible inside the error banner').toBeVisible();
    await expect(this.activationLink(), 'activation link is a real anchor element').toHaveJSProperty('tagName', 'A');
    await expect(this.activationLink(), 'activation link has non-empty accessible text').not.toHaveText('');
  }

  async assertLoginAndPasswordFieldsAreCleared(): Promise<void> {
    await expect(this.loginInput(), 'login field is cleared').toHaveValue('');
    await expect(this.passwordInput(), 'password field is cleared').toHaveValue('');
  }

  async assertForgotPasswordIsAbsent(): Promise<void> {
    await expect(this.page.getByText('Forgot password?'), 'no "Forgot password" element is present').toHaveCount(0);
  }

  async assertLoginFieldErrorShown(): Promise<void> {
    await expect(this.loginError(), 'per-field error under the login input is visible').toBeVisible();
    await expect(this.loginError(), 'login field error shows the exact login validation message').toHaveText(
      LOGIN_FIELD_ERROR_MESSAGE,
    );
  }

  async assertPasswordFieldErrorShown(): Promise<void> {
    await expect(this.passwordError(), 'per-field error under the password input is visible').toBeVisible();
    await expect(this.passwordError(), 'password field error shows the exact password validation message').toHaveText(
      PASSWORD_FIELD_ERROR_MESSAGE,
    );
  }

  async assertErrorBannerIsAbsent(): Promise<void> {
    await expect(this.errorBanner(), 'global error banner is absent on a field-validation failure').toHaveCount(0);
  }

  private loginInput(): Locator {
    return this.page.getByTestId(TEST_ID.loginInput);
  }

  private errorBanner(): Locator {
    return this.page.getByTestId(TEST_ID.errorBanner);
  }

  private activationLink(): Locator {
    return this.page.getByTestId(TEST_ID.activationLink);
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

  private loginError(): Locator {
    return this.page.getByTestId(TEST_ID.loginError);
  }

  private passwordError(): Locator {
    return this.page.getByTestId(TEST_ID.passwordError);
  }
}
