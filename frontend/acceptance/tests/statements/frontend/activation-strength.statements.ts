import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  passwordInput: 'activation-password-input',
} as const;

const RULE_TESTID_PREFIX = 'complexity-rule-';
const MET_ATTRIBUTE = 'data-met';

const PARTIAL_PASSWORD = 'weak';
const FULL_PASSWORD = 'Str0ng-P@ssw0rd!';

const RULE_KEY = {
  length: 'length',
  uppercase: 'uppercase',
  lowercase: 'lowercase',
  digit: 'digit',
  special: 'special',
  noSpaces: 'no-spaces',
} as const;

const ALL_RULE_KEYS = [
  RULE_KEY.length,
  RULE_KEY.uppercase,
  RULE_KEY.lowercase,
  RULE_KEY.digit,
  RULE_KEY.special,
  RULE_KEY.noSpaces,
] as const;

const PARTIAL_MET_KEYS = [RULE_KEY.lowercase, RULE_KEY.noSpaces] as const;
const PARTIAL_UNMET_KEYS = [RULE_KEY.length, RULE_KEY.uppercase, RULE_KEY.digit, RULE_KEY.special] as const;

export class ActivationStrengthStatements {
  constructor(private readonly page: Page) {}

  async typePartiallySatisfyingPassword(): Promise<void> {
    await this.passwordInput().fill(PARTIAL_PASSWORD);
  }

  async typeFullySatisfyingPassword(): Promise<void> {
    await this.passwordInput().fill(FULL_PASSWORD);
  }

  async assertOnlySatisfiedRulesAreMet(): Promise<void> {
    for (const key of PARTIAL_MET_KEYS) {
      await this.assertRuleMet(key);
    }
    for (const key of PARTIAL_UNMET_KEYS) {
      await this.assertRuleUnmet(key);
    }
  }

  async assertAllRulesAreMet(): Promise<void> {
    for (const key of ALL_RULE_KEYS) {
      await this.assertRuleMet(key);
    }
  }

  private async assertRuleMet(key: string): Promise<void> {
    await expect(this.rule(key), `complexity rule "${key}" is satisfied (data-met=true)`).toHaveAttribute(
      MET_ATTRIBUTE,
      'true',
    );
  }

  private async assertRuleUnmet(key: string): Promise<void> {
    await expect(this.rule(key), `complexity rule "${key}" is not satisfied (data-met=false)`).toHaveAttribute(
      MET_ATTRIBUTE,
      'false',
    );
  }

  private rule(key: string): Locator {
    return this.page.getByTestId(`${RULE_TESTID_PREFIX}${key}`);
  }

  private passwordInput(): Locator {
    return this.page.getByTestId(TEST_ID.passwordInput);
  }
}
