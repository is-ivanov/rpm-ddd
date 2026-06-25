import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  userMenu: 'user-menu',
  userMenuName: 'user-menu-name',
  userMenuEmail: 'user-menu-email',
  userMenuLogout: 'user-menu-logout',
} as const;

const LOGOUT_ACTION_TEXT = 'Log out';

export class UserMenuStatements {
  constructor(private readonly page: Page) {}

  async assertMenuIsOpen(): Promise<void> {
    await expect(this.userMenu(), 'user menu is open').toBeVisible();
  }

  async assertMenuShowsName(name: string): Promise<void> {
    await expect(this.userMenuName(), 'user menu name is visible').toBeVisible();
    await expect(this.userMenuName(), 'user menu shows the full name').toHaveText(name);
  }

  async assertMenuShowsEmail(email: string): Promise<void> {
    await expect(this.userMenuEmail(), 'user menu email is visible').toBeVisible();
    await expect(this.userMenuEmail(), 'user menu shows the email').toHaveText(email);
  }

  async assertMenuShowsLogoutAction(): Promise<void> {
    await expect(this.userMenuLogout(), 'logout action is visible').toBeVisible();
    await expect(this.userMenuLogout(), 'logout action text is exactly "Log out"').toHaveText(LOGOUT_ACTION_TEXT);
  }

  async clickLogout(): Promise<void> {
    await this.userMenuLogout().click();
  }

  private userMenu(): Locator {
    return this.page.getByTestId(TEST_ID.userMenu);
  }

  private userMenuName(): Locator {
    return this.page.getByTestId(TEST_ID.userMenuName);
  }

  private userMenuEmail(): Locator {
    return this.page.getByTestId(TEST_ID.userMenuEmail);
  }

  private userMenuLogout(): Locator {
    return this.page.getByTestId(TEST_ID.userMenuLogout);
  }
}
