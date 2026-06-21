import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  passwordInput: 'activation-password-input',
  passwordStrength: 'password-strength',
} as const;

const STRENGTH_ATTRIBUTE = 'data-strength';

export class ActivationStrengthStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl: string,
  ) {}

  async navigateToActivationPageWithToken(token: string): Promise<void> {
    await this.page.goto(`${this.appUrl}/activate?token=${token}`);
    await expect(this.passwordInput(), 'activation password input is visible on page load').toBeVisible();
  }

  async typePassword(password: string): Promise<void> {
    await this.passwordInput().fill(password);
  }

  async assertStrengthIndicatorShowsWeak(): Promise<void> {
    await expect(this.strengthIndicator(), 'password strength indicator is visible').toBeVisible();
    await expect(this.strengthIndicator(), 'strength indicator reflects a weak password').toHaveAttribute(
      STRENGTH_ATTRIBUTE,
      'weak',
    );
  }

  async assertStrengthIndicatorUpdatesToStrong(): Promise<void> {
    await expect(this.strengthIndicator(), 'password strength indicator is visible').toBeVisible();
    await expect(
      this.strengthIndicator(),
      'strength indicator updates to strong in real-time as the value changes',
    ).toHaveAttribute(STRENGTH_ATTRIBUTE, 'strong');
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }

  private strengthIndicator(): Locator {
    return this.page.getByTestId(TEST_ID.passwordStrength);
  }
}
