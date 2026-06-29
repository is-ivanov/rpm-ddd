import { expect, type Locator, type Page } from '@playwright/test';
import { LOGINS_ASCENDING, LOGINS_DESCENDING, STATUSES_IN_LIFECYCLE_ORDER } from '../support/admin-users-fixture';

const TEST_ID = {
  grid: 'users-grid',
  loginHeader: 'users-grid-header-login',
  statusHeader: 'users-grid-header-status',
  loginCell: 'users-cell-login',
  statusBadge: 'users-status-badge',
} as const;

export class UsersGridSortStatements {
  constructor(private readonly page: Page) {}

  async clickLoginHeader(): Promise<void> {
    await this.loginHeader().click();
  }

  async clickStatusHeader(): Promise<void> {
    await this.statusHeader().click();
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

  private loginHeader(): Locator {
    return this.page.getByTestId(TEST_ID.loginHeader);
  }

  private statusHeader(): Locator {
    return this.page.getByTestId(TEST_ID.statusHeader);
  }

  private loginCells(): Locator {
    return this.grid().getByTestId(TEST_ID.loginCell);
  }

  private statusBadgeCells(): Locator {
    return this.grid().getByTestId(TEST_ID.statusBadge);
  }

  private grid(): Locator {
    return this.page.getByTestId(TEST_ID.grid);
  }
}
