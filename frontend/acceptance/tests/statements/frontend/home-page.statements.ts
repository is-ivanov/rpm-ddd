import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  homePage: 'home-page',
  welcomeLogo: 'welcome-logo',
  welcomeTagline: 'welcome-tagline',
  welcomeLoginButton: 'welcome-login-button',
  dashboardShell: 'dashboard-shell',
} as const;

const WELCOME_TAGLINE = 'Удалённый мониторинг пациентов';
const LOGIN_BUTTON_TEXT = 'Войти';

export class HomePageStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl = '',
  ) {}

  async navigateToHomePage(): Promise<void> {
    await this.page.goto(`${this.appUrl}/`);
  }

  async assertHomePageIsVisible(): Promise<void> {
    await expect(this.homePage(), 'home page is visible').toBeVisible();
  }

  async assertWelcomeLogoIsVisible(): Promise<void> {
    await expect(this.welcomeLogo(), 'welcome logo is visible').toBeVisible();
    await expect(this.welcomeLogo(), 'welcome logo shows "RPM"').toHaveText('RPM');
  }

  async assertTaglineIsVisible(): Promise<void> {
    await expect(this.welcomeTagline(), 'tagline is visible').toBeVisible();
    await expect(this.welcomeTagline(), 'tagline shows the exact Russian text').toHaveText(WELCOME_TAGLINE);
  }

  async assertLoginButtonIsVisible(): Promise<void> {
    await expect(this.welcomeLoginButton(), 'login button is visible').toBeVisible();
    await expect(this.welcomeLoginButton(), 'login button text is exactly "Войти"').toHaveText(LOGIN_BUTTON_TEXT);
  }

  async assertDashboardShellIsAbsent(): Promise<void> {
    await expect(this.dashboardShell(), 'dashboard shell is not displayed for unauthenticated users').toHaveCount(0);
  }

  private homePage(): Locator {
    return this.page.getByTestId(TEST_ID.homePage);
  }

  private welcomeLogo(): Locator {
    return this.page.getByTestId(TEST_ID.welcomeLogo);
  }

  private welcomeTagline(): Locator {
    return this.page.getByTestId(TEST_ID.welcomeTagline);
  }

  private welcomeLoginButton(): Locator {
    return this.page.getByTestId(TEST_ID.welcomeLoginButton);
  }

  private dashboardShell(): Locator {
    return this.page.getByTestId(TEST_ID.dashboardShell);
  }
}
