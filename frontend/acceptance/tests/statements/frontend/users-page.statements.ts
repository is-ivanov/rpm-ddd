import { expect, type Locator, type Page } from '@playwright/test';
import {
  type AuditActorField,
  EXPECTED_USER_ROWS,
  FULL_NAME_FILTER_TERM,
  FULL_NAMES_MATCHING_FILTER,
  SEED_ACTOR_CELLS,
  SEED_ACTOR_DISPLAY,
} from '../support/admin-users-fixture';
import { NEW_PENDING_USER_ROW, NEW_USER_INPUT } from '../support/register-user-fixture';
import { USERS_GRID_TEST_ID, UsersGridLocators } from '../support/users-grid-locators';

const TEST_ID = {
  usersPage: 'users-page',
  registerUserButton: 'register-user-button',
} as const;

const REGISTER_USER_BUTTON_TEXT = 'Register user';
const PENDING_STATUS_LABEL = 'Pending';

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
  name: USERS_GRID_TEST_ID.nameCell,
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
  private readonly gridLocators: UsersGridLocators;

  constructor(private readonly page: Page) {
    this.gridLocators = new UsersGridLocators(page);
  }

  async assertUsersPageIsVisible(): Promise<void> {
    await expect(this.usersPage(), 'Users page content is visible').toBeVisible();
  }

  async assertRegisterUserButtonIsVisible(): Promise<void> {
    await expect(this.registerUserButton(), '"Register user" button is visible').toBeVisible();
    await expect(this.registerUserButton(), 'button text is exactly "Register user"').toHaveText(
      REGISTER_USER_BUTTON_TEXT,
    );
  }

  async clickRegisterUserButton(): Promise<void> {
    await this.registerUserButton().click();
  }

  async assertGridIsVisible(): Promise<void> {
    await expect(this.gridLocators.grid(), 'users grid is visible').toBeVisible();
    await expect(this.gridLocators.rows(), 'grid shows one row per user from the API').toHaveCount(
      EXPECTED_USER_ROWS.length,
    );
  }

  async assertLoadingStateIsVisible(): Promise<void> {
    await expect(
      this.gridLocators.loading(),
      'grid loading indicator is visible while the request is in flight',
    ).toBeVisible();
    await expect(this.gridLocators.rows(), 'no rows render while the request is still in flight').toHaveCount(0);
  }

  async assertRowsRenderAfterResponse(): Promise<void> {
    await expect(this.gridLocators.loading(), 'loading indicator disappears once the response arrives').toHaveCount(0);
    await expect(this.gridLocators.grid(), 'users grid is visible after the response arrives').toBeVisible();
    await expect(this.gridLocators.rows(), 'grid shows one row per user once the response arrives').toHaveCount(
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

  async assertFullNameFilterIsVisible(): Promise<void> {
    await expect(
      this.gridLocators.nameFilter(),
      'the Full name column filter input is visible in the grid',
    ).toBeVisible();
  }

  async enterFullNameFilter(): Promise<void> {
    await this.gridLocators.nameFilter().fill(FULL_NAME_FILTER_TERM);
  }

  async assertOnlyMatchingFullNamesRemain(): Promise<void> {
    await expect(
      this.gridLocators.rows(),
      'only rows whose full name contains the filter text remain visible',
    ).toHaveCount(FULL_NAMES_MATCHING_FILTER.length);
    await expect(
      this.gridLocators.nameCells(),
      'remaining rows are exactly the full names that contain the filter text',
    ).toHaveText([...FULL_NAMES_MATCHING_FILTER]);
  }

  async assertNewPendingUserRowIsVisible(): Promise<void> {
    const expected = NEW_PENDING_USER_ROW;
    await expect(this.gridLocators.rows(), 'grid refreshed with one extra row').toHaveCount(
      EXPECTED_USER_ROWS.length + 1,
    );
    const newRow = this.newUserRow();
    await expect(newRow, 'exactly one new-user row is visible').toHaveCount(1);
    await expect(newRow.getByTestId(CELL.name), 'new row full name').toHaveText(expected.name);
    await expect(newRow.getByTestId(CELL.login), 'new row login').toHaveText(expected.login);
    await expect(newRow.getByTestId(CELL.email), 'new row email').toHaveText(expected.email);
    const badge = newRow.getByTestId(CELL.statusBadge);
    await expect(badge, 'new row status badge is visible').toBeVisible();
    await expect(badge, 'new row status badge reads "Pending"').toHaveText(expected.status);
    await expect(newRow.getByTestId(CELL.createdBy), 'new row created-by actor').toHaveText(expected.createdBy);
    await expect(newRow.getByTestId(CELL.updatedBy), 'new row updated-by actor').toHaveText(expected.updatedBy);
  }

  // Full-stack journey: the grid runs against a persistent Postgres that accumulates
  // fsuser_* rows across runs, so the total row count is non-deterministic. Assert the
  // newly-created user by its unique login instead of a fixed count/fixture.
  async assertUserAppearsWithPendingStatus(login: string): Promise<void> {
    const row = this.gridLocators.rows().filter({ hasText: login });
    await expect(row, `exactly one grid row for the newly created user "${login}"`).toHaveCount(1);
    await expect(row.getByTestId(CELL.login), 'new row login matches the created identity').toHaveText(login);
    const badge = row.getByTestId(CELL.statusBadge);
    await expect(badge, 'new row status badge is visible').toBeVisible();
    await expect(badge, 'new row status badge reads "Pending"').toHaveText(PENDING_STATUS_LABEL);
  }

  async assertGridRowCountUnchanged(): Promise<void> {
    await expect(
      this.gridLocators.rows(),
      'the grid still shows exactly the seeded rows — cancelling added no row',
    ).toHaveCount(EXPECTED_USER_ROWS.length);
    await expect(
      this.gridLocators.rows().filter({ hasText: NEW_USER_INPUT.login }),
      'no row for the cancelled input login appears in the grid',
    ).toHaveCount(0);
  }

  private newUserRow(): Locator {
    return this.gridLocators.rows().filter({ hasText: NEW_PENDING_USER_ROW.login });
  }

  private cell(rowIndex: number, cellTestId: string): Locator {
    return this.gridLocators.rows().nth(rowIndex).getByTestId(cellTestId);
  }

  private usersPage(): Locator {
    return this.page.getByTestId(TEST_ID.usersPage);
  }

  private registerUserButton(): Locator {
    return this.page.getByTestId(TEST_ID.registerUserButton);
  }
}
