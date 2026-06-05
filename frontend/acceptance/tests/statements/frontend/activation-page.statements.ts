import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  passwordInput: 'activation-password-input',
  confirmPasswordInput: 'activation-confirm-password-input',
  complexityRules: 'password-complexity-rules',
  complexityRuleItem: 'password-complexity-rule',
  submitButton: 'activate-button',
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
}
