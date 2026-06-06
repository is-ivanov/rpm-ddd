import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  passwordInput: 'activation-password-input',
  confirmPasswordInput: 'activation-confirm-password-input',
  complexityRules: 'password-complexity-rules',
  complexityRuleItem: 'password-complexity-rule',
  submitButton: 'activate-button',
  successScreen: 'activation-success',
  successIcon: 'activation-success-icon',
  successTitle: 'activation-success-title',
  goToSignInButton: 'go-to-sign-in-button',
} as const;

const COMPLEXITY_RULES = [
  'At least 12 characters',
  'At least one uppercase letter',
  'At least one lowercase letter',
  'At least one digit',
  'At least one special character',
  'No spaces',
] as const;

export class ActivationPageStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async navigateToActivationPageWithToken(token: string): Promise<void> {
    await this.page.goto(`${this.appUrl}/activate?token=${token}`);
  }

  async assertPasswordFieldIsVisible(): Promise<void> {
    await expect(this.passwordInput(), 'password input field is visible').toBeVisible();
    await expect(this.passwordInput(), 'password field masks entered text').toHaveAttribute('type', 'password');
  }

  async assertConfirmPasswordFieldIsVisible(): Promise<void> {
    await expect(this.confirmPasswordInput(), 'confirm password input field is visible').toBeVisible();
    await expect(this.confirmPasswordInput(), 'confirm password field masks entered text').toHaveAttribute(
      'type',
      'password',
    );
  }

  async assertComplexityRulesAreDisplayed(): Promise<void> {
    await expect(this.complexityRules(), 'password complexity rules list is visible').toBeVisible();
    await expect(this.complexityRuleItems(), 'all complexity rules are displayed').toHaveCount(COMPLEXITY_RULES.length);
    await expect(this.complexityRuleItems(), 'complexity rules show the exact required criteria').toHaveText([
      ...COMPLEXITY_RULES,
    ]);
  }

  async assertSubmitButtonIsVisible(): Promise<void> {
    await expect(this.submitButton(), 'submit button is visible').toBeVisible();
    await expect(this.submitButton(), 'submit button has text "Activate Account"').toHaveText('Activate Account');
  }

  async enterPassword(password: string): Promise<void> {
    await this.passwordInput().fill(password);
  }

  async enterConfirmPassword(password: string): Promise<void> {
    await this.confirmPasswordInput().fill(password);
  }

  async clickActivateButton(): Promise<void> {
    await this.submitButton().click();
  }

  async assertSuccessIconIsVisible(): Promise<void> {
    await expect(this.successScreen(), 'activation success screen is visible').toBeVisible();
    await expect(this.successIcon(), 'green check icon is visible on the success screen').toBeVisible();
    await expect(this.successIcon().locator('svg'), 'green check icon renders its SVG content').toBeVisible();
    await expect(this.successIcon(), 'green check icon is not empty').not.toBeEmpty();
  }

  async assertSuccessMessageIsDisplayed(): Promise<void> {
    await expect(this.successTitle(), 'success message is visible').toBeVisible();
    await expect(this.successTitle(), 'success message reads "Account Activated!"').toHaveText('Account Activated!');
  }

  async assertGoToSignInButtonIsVisible(): Promise<void> {
    await expect(this.goToSignInButton(), '"Go to Sign In" button is visible').toBeVisible();
    await expect(this.goToSignInButton(), 'button has text "Go to Sign In"').toHaveText('Go to Sign In');
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }

  private confirmPasswordInput(): Locator {
    return this.page.getByTestId(TEST_ID.confirmPasswordInput);
  }

  private complexityRules(): Locator {
    return this.page.getByTestId(TEST_ID.complexityRules);
  }

  private complexityRuleItems(): Locator {
    return this.complexityRules().getByTestId(TEST_ID.complexityRuleItem);
  }

  private submitButton(): Locator {
    return this.page.getByTestId(TEST_ID.submitButton);
  }

  private successScreen(): Locator {
    return this.page.getByTestId(TEST_ID.successScreen);
  }

  private successIcon(): Locator {
    return this.page.getByTestId(TEST_ID.successIcon);
  }

  private successTitle(): Locator {
    return this.page.getByTestId(TEST_ID.successTitle);
  }

  private goToSignInButton(): Locator {
    return this.page.getByTestId(TEST_ID.goToSignInButton);
  }
}
