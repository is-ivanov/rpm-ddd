import { expect, type Page } from '@playwright/test';

export class LoginPageStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async navigateToLoginPage(): Promise<void> {
    await this.page.goto(`${this.appUrl}/login`);
  }

  async assertLoginFieldIsVisible(): Promise<void> {
    await expect(
      this.page.getByTestId('login-input'),
      'login input field is visible',
    ).toBeVisible();
  }

  async assertPasswordFieldIsVisible(): Promise<void> {
    await expect(
      this.page.getByTestId('password-input'),
      'password input field is visible',
    ).toBeVisible();
  }

  async assertPasswordFieldMasksText(): Promise<void> {
    await expect(
      this.page.getByTestId('password-input'),
      'password field masks entered text',
    ).toHaveAttribute('type', 'password');
  }

  async assertSubmitButtonIsVisible(): Promise<void> {
    const submitButton = this.page.getByTestId('submit-button');
    await expect(submitButton, 'submit button is visible').toBeVisible();
    await expect(submitButton, 'submit button has text "Sign In"').toHaveText('Sign In');
  }
}
