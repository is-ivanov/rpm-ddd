import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  trigger: 'app-version-trigger',
  popover: 'app-version-popover',
  number: 'app-version-number',
  commit: 'app-version-commit',
  buildTime: 'app-version-build-time',
} as const;

export class AppVersionStatements {
  constructor(private readonly page: Page) {}

  async assertHelpIconIsVisible(): Promise<void> {
    await expect(this.trigger(), 'header help icon that opens the version popover is visible').toBeVisible();
  }

  async clickHelpIcon(): Promise<void> {
    await this.trigger().click();
  }

  async assertPopoverIsVisible(): Promise<void> {
    await expect(this.popover(), 'version popover is visible after clicking the help icon').toBeVisible();
  }

  async assertPopoverShowsVersion(version: string): Promise<void> {
    await expect(this.number(), 'popover shows the deployed version').toBeVisible();
    await expect(this.number(), 'popover shows the exact deployed version').toHaveText(version);
  }

  async assertPopoverShowsCommit(commit: string): Promise<void> {
    await expect(this.commit(), 'popover shows the deployed commit').toBeVisible();
    await expect(this.commit(), 'popover shows the exact deployed commit').toHaveText(commit);
  }

  async assertBuildTimeIsVisible(): Promise<void> {
    await expect(this.buildTime(), 'popover shows the build time').toBeVisible();
    await expect(this.buildTime(), 'popover build time is not empty').not.toBeEmpty();
  }

  private trigger(): Locator {
    return this.page.getByTestId(TEST_ID.trigger);
  }

  private popover(): Locator {
    return this.page.getByTestId(TEST_ID.popover);
  }

  private number(): Locator {
    return this.page.getByTestId(TEST_ID.number);
  }

  private commit(): Locator {
    return this.page.getByTestId(TEST_ID.commit);
  }

  private buildTime(): Locator {
    return this.page.getByTestId(TEST_ID.buildTime);
  }
}
