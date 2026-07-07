import { expect, type Locator, type Page } from '@playwright/test';
import {
  LOGINS_ASCENDING,
  LOGINS_BY_CREATED_INSTANT_ASCENDING,
  LOGINS_BY_CREATED_INSTANT_DESCENDING,
  LOGINS_DESCENDING,
  STATUSES_IN_LIFECYCLE_ORDER,
} from '../support/admin-users-sort.fixture';
import { UsersGridLocators } from '../support/users-grid-locators';

const TEST_ID = {
  loginHeader: 'users-grid-header-login',
  statusHeader: 'users-grid-header-status',
  createdHeader: 'users-grid-header-created',
  loginCell: 'users-cell-login',
  statusBadge: 'users-status-badge',
} as const;

export class UsersGridSortStatements {
  private readonly grid: UsersGridLocators;

  constructor(private readonly page: Page) {
    this.grid = new UsersGridLocators(page);
  }

  async clickLoginHeader(): Promise<void> {
    await this.loginHeader().click();
  }

  async clickStatusHeader(): Promise<void> {
    await this.statusHeader().click();
  }

  async clickCreatedHeader(): Promise<void> {
    await this.createdHeader().click();
  }

  async assertLoginsSortedAscending(): Promise<void> {
    await expect(this.loginCells(), 'login cells are sorted ascending after the first Login-header click').toHaveText([
      ...LOGINS_ASCENDING,
    ]);
  }

  async assertLoginsSortedDescending(): Promise<void> {
    await expect(this.loginCells(), 'login cells are sorted descending after a second Login-header click').toHaveText([
      ...LOGINS_DESCENDING,
    ]);
  }

  async assertStatusesSortedByLifecycleOrder(): Promise<void> {
    await expect(
      this.statusBadgeCells(),
      'status badges are ordered by lifecycle (Pending, Active, Locked, Inactive), not alphabetically',
    ).toHaveText([...STATUSES_IN_LIFECYCLE_ORDER]);
  }

  async assertRowsSortedByCreatedInstantAscending(): Promise<void> {
    await expect(
      this.loginCells(),
      'rows are ordered by the underlying Created instant ascending (oldest first), not the relative-time label',
    ).toHaveText([...LOGINS_BY_CREATED_INSTANT_ASCENDING]);
  }

  async assertRowsSortedByCreatedInstantDescending(): Promise<void> {
    await expect(
      this.loginCells(),
      'rows are ordered by the underlying Created instant descending after a second Created-header click',
    ).toHaveText([...LOGINS_BY_CREATED_INSTANT_DESCENDING]);
  }

  private loginHeader(): Locator {
    return this.page.getByTestId(TEST_ID.loginHeader);
  }

  private createdHeader(): Locator {
    return this.page.getByTestId(TEST_ID.createdHeader);
  }

  private statusHeader(): Locator {
    return this.page.getByTestId(TEST_ID.statusHeader);
  }

  private loginCells(): Locator {
    return this.grid.grid().getByTestId(TEST_ID.loginCell);
  }

  private statusBadgeCells(): Locator {
    return this.grid.grid().getByTestId(TEST_ID.statusBadge);
  }
}
