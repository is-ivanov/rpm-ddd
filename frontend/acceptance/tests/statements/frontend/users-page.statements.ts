import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  usersPage: 'users-page',
  registerUserButton: 'register-user-button',
} as const;

const REGISTER_USER_BUTTON_TEXT = 'Register user';

export class UsersPageStatements {
  constructor(private readonly page: Page) {}

  async assertUsersPageIsVisible(): Promise<void> {
    await expect(this.usersPage(), 'Users page content is visible').toBeVisible();
  }

  async assertRegisterUserButtonIsVisible(): Promise<void> {
    await expect(this.registerUserButton(), '"Register user" button is visible').toBeVisible();
    await expect(this.registerUserButton(), 'button text is exactly "Register user"').toHaveText(
      REGISTER_USER_BUTTON_TEXT,
    );
  }

  private usersPage(): Locator {
    return this.page.getByTestId(TEST_ID.usersPage);
  }

  private registerUserButton(): Locator {
    return this.page.getByTestId(TEST_ID.registerUserButton);
  }
}
