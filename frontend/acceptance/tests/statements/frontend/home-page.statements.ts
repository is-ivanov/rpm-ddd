import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  homePage: 'home-page',
  homeTitle: 'home-title',
} as const;

export class HomePageStatements {
  constructor(private readonly page: Page) {}

  async assertHomePageIsVisible(): Promise<void> {
    await expect(this.homePage(), 'home page is visible').toBeVisible();
    await expect(this.homeTitle(), 'home title shows "RPM"').toHaveText('RPM');
  }

  private homePage(): Locator {
    return this.page.getByTestId(TEST_ID.homePage);
  }

  private homeTitle(): Locator {
    return this.page.getByTestId(TEST_ID.homeTitle);
  }
}
