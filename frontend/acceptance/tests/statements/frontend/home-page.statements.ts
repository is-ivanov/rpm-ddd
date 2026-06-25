import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  homePage: 'home-page',
  welcomeLogo: 'welcome-logo',
  welcomeTagline: 'welcome-tagline',
  welcomeLoginButton: 'welcome-login-button',
  dashboardShell: 'dashboard-shell',
  topbarLogo: 'topbar-logo',
  userAvatar: 'user-avatar',
  userName: 'user-name',
  dashboardSidebar: 'dashboard-sidebar',
  pageTitle: 'page-title',
  dashboardPlaceholder: 'dashboard-placeholder',
} as const;

const BRAND_LOGO_TEXT = 'RPM';
const WELCOME_TAGLINE = 'Remote Patient Monitoring';
const LOGIN_BUTTON_TEXT = 'Sign in';
const PAGE_TITLE_TEXT = 'Home';

export class HomePageStatements {
  constructor(
    private readonly page: Page,
    private readonly appUrl = '',
  ) {}

  async navigateToHomePage(): Promise<void> {
    await this.page.goto(`${this.appUrl}/`);
  }

  async assertNavigatedToHomeUrl(): Promise<void> {
    await expect(this.page, 'browser is navigated to the home page URL').toHaveURL(`${this.appUrl}/`);
  }

  async assertHomePageIsVisible(): Promise<void> {
    await expect(this.homePage(), 'home page is visible').toBeVisible();
  }

  async assertWelcomeLogoIsVisible(): Promise<void> {
    await expect(this.welcomeLogo(), 'welcome logo is visible').toBeVisible();
    await expect(this.welcomeLogo(), 'welcome logo shows "RPM"').toHaveText(BRAND_LOGO_TEXT);
  }

  async assertTaglineIsVisible(): Promise<void> {
    await expect(this.welcomeTagline(), 'tagline is visible').toBeVisible();
    await expect(this.welcomeTagline(), 'tagline shows the exact text').toHaveText(WELCOME_TAGLINE);
  }

  async assertLoginButtonIsVisible(): Promise<void> {
    await expect(this.welcomeLoginButton(), 'login button is visible').toBeVisible();
    await expect(this.welcomeLoginButton(), 'login button text is exactly "Sign in"').toHaveText(LOGIN_BUTTON_TEXT);
  }

  async clickLoginButton(): Promise<void> {
    await this.welcomeLoginButton().click();
  }

  async assertDashboardShellIsAbsent(): Promise<void> {
    await expect(this.dashboardShell(), 'dashboard shell is not displayed for unauthenticated users').toHaveCount(0);
  }

  async assertDashboardShellIsVisible(): Promise<void> {
    await expect(this.dashboardShell(), 'dashboard shell is visible for authenticated users').toBeVisible();
  }

  async assertTopbarLogoIsVisible(): Promise<void> {
    await expect(this.topbarLogo(), 'top bar logo is visible').toBeVisible();
    await expect(this.topbarLogo(), 'top bar logo shows "RPM"').toHaveText(BRAND_LOGO_TEXT);
  }

  async clickUserAvatar(): Promise<void> {
    await this.userAvatar().click();
  }

  async assertUserAvatarShowsInitials(initials: string): Promise<void> {
    await expect(this.userAvatar(), 'user avatar is visible').toBeVisible();
    await expect(this.userAvatar(), 'user avatar shows the derived initials').toHaveText(initials);
  }

  async assertUserNameIsVisible(name: string): Promise<void> {
    await expect(this.userName(), 'user name is visible').toBeVisible();
    await expect(this.userName(), 'user name shows the full name').toHaveText(name);
  }

  async assertSidebarIsVisible(): Promise<void> {
    await expect(this.dashboardSidebar(), 'navigation sidebar is visible').toBeVisible();
  }

  async assertPageTitleIsVisible(): Promise<void> {
    await expect(this.pageTitle(), 'page title is visible').toBeVisible();
    await expect(this.pageTitle(), 'page title shows "Home"').toHaveText(PAGE_TITLE_TEXT);
  }

  async assertPlaceholderContentIsVisible(): Promise<void> {
    await expect(this.dashboardPlaceholder(), 'placeholder dashboard content is visible').toBeVisible();
    await expect(this.dashboardPlaceholder(), 'placeholder dashboard content is not empty').not.toBeEmpty();
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

  private topbarLogo(): Locator {
    return this.page.getByTestId(TEST_ID.topbarLogo);
  }

  private userAvatar(): Locator {
    return this.page.getByTestId(TEST_ID.userAvatar);
  }

  private userName(): Locator {
    return this.page.getByTestId(TEST_ID.userName);
  }

  private dashboardSidebar(): Locator {
    return this.page.getByTestId(TEST_ID.dashboardSidebar);
  }

  private pageTitle(): Locator {
    return this.page.getByTestId(TEST_ID.pageTitle);
  }

  private dashboardPlaceholder(): Locator {
    return this.page.getByTestId(TEST_ID.dashboardPlaceholder);
  }
}
