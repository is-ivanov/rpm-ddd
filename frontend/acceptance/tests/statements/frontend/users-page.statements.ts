import { expect, type Locator, type Page } from '@playwright/test';
import {
  type AuditActorField,
  EXPECTED_USER_ROWS,
  SEED_ACTOR_CELLS,
  SEED_ACTOR_DISPLAY,
} from '../support/admin-users-fixture';

const TEST_ID = {
  usersPage: 'users-page',
  registerUserButton: 'register-user-button',
  grid: 'users-grid',
  row: 'users-grid-row',
  loading: 'users-grid-loading',
} as const;

const REGISTER_USER_BUTTON_TEXT = 'Register user';

const COLUMN_HEADERS = [
  { testId: 'users-grid-header-name', text: 'Full name' },
  { testId: 'users-grid-header-login', text: 'Login' },
  { testId: 'users-grid-header-email', text: 'Email' },
  { testId: 'users-grid-header-status', text: 'Status' },
  { testId: 'users-grid-header-created', text: 'Created' },
  { testId: 'users-grid-header-created-by', text: 'Created by' },
  { testId: 'users-grid-header-updated', text: 'Updated' },
  { testId: 'users-grid-header-updated-by', text: 'Updated by' },
] as const;

const CELL = {
  name: 'users-cell-name',
  login: 'users-cell-login',
  email: 'users-cell-email',
  statusBadge: 'users-status-badge',
  created: 'users-cell-created',
  createdBy: 'users-cell-created-by',
  updated: 'users-cell-updated',
  updatedBy: 'users-cell-updated-by',
} as const;

const ACTOR_CELL: Record<AuditActorField, string> = {
  createdBy: CELL.createdBy,
  updatedBy: CELL.updatedBy,
};

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

  async assertGridIsVisible(): Promise<void> {
    await expect(this.grid(), 'users grid is visible').toBeVisible();
    await expect(this.rows(), 'grid shows one row per user from the API').toHaveCount(EXPECTED_USER_ROWS.length);
  }

  async assertLoadingStateIsVisible(): Promise<void> {
    await expect(
      this.loadingIndicator(),
      'grid loading indicator is visible while the request is in flight',
    ).toBeVisible();
    await expect(this.rows(), 'no rows render while the request is still in flight').toHaveCount(0);
  }

  async assertRowsRenderAfterResponse(): Promise<void> {
    await expect(this.loadingIndicator(), 'loading indicator disappears once the response arrives').toHaveCount(0);
    await expect(this.grid(), 'users grid is visible after the response arrives').toBeVisible();
    await expect(this.rows(), 'grid shows one row per user once the response arrives').toHaveCount(
      EXPECTED_USER_ROWS.length,
    );
  }

  async assertAllColumnHeadersAreDisplayed(): Promise<void> {
    for (const header of COLUMN_HEADERS) {
      const headerCell = this.page.getByTestId(header.testId);
      await expect(headerCell, `column header "${header.text}" is visible`).toBeVisible();
      await expect(headerCell, `column header text is exactly "${header.text}"`).toHaveText(header.text);
    }
  }

  async assertEachRowShowsNameLoginEmail(): Promise<void> {
    for (const [index, expected] of EXPECTED_USER_ROWS.entries()) {
      await expect(this.cell(index, CELL.name), `row ${index} full name`).toHaveText(expected.name);
      await expect(this.cell(index, CELL.login), `row ${index} login`).toHaveText(expected.login);
      await expect(this.cell(index, CELL.email), `row ${index} email`).toHaveText(expected.email);
    }
  }

  async assertEachRowShowsStatusBadge(): Promise<void> {
    for (const [index, expected] of EXPECTED_USER_ROWS.entries()) {
      const badge = this.cell(index, CELL.statusBadge);
      await expect(badge, `row ${index} status badge is visible`).toBeVisible();
      await expect(badge, `row ${index} status badge text is "${expected.status}"`).toHaveText(expected.status);
    }
  }

  async assertAuditActorsAreAbbreviated(): Promise<void> {
    for (const [index, expected] of EXPECTED_USER_ROWS.entries()) {
      await expect(this.cell(index, CELL.createdBy), `row ${index} created-by actor`).toHaveText(expected.createdBy);
      await expect(this.cell(index, CELL.updatedBy), `row ${index} updated-by actor`).toHaveText(expected.updatedBy);
    }
  }

  async assertSeedActorIsShownAsSystem(): Promise<void> {
    for (const { rowIndex, field } of SEED_ACTOR_CELLS) {
      await expect(
        this.cell(rowIndex, ACTOR_CELL[field]),
        `row ${rowIndex} seed ${field} shows "${SEED_ACTOR_DISPLAY}"`,
      ).toHaveText(SEED_ACTOR_DISPLAY);
    }
  }

  private cell(rowIndex: number, cellTestId: string): Locator {
    return this.rows().nth(rowIndex).getByTestId(cellTestId);
  }

  private rows(): Locator {
    return this.grid().getByTestId(TEST_ID.row);
  }

  private loadingIndicator(): Locator {
    return this.page.getByTestId(TEST_ID.loading);
  }

  private grid(): Locator {
    return this.page.getByTestId(TEST_ID.grid);
  }

  private usersPage(): Locator {
    return this.page.getByTestId(TEST_ID.usersPage);
  }

  private registerUserButton(): Locator {
    return this.page.getByTestId(TEST_ID.registerUserButton);
  }
}
