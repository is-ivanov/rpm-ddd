import { expect, type Locator, type Page } from '@playwright/test';
import { FULL_NAMES_MATCHING_STATUS_FILTER, STATUS_FILTER_SELECTION } from '../support/admin-users-fixture';
import { STATUSES_IN_LIFECYCLE_ORDER } from '../support/admin-users-sort.fixture';

const TEST_ID = {
  grid: 'users-grid',
  row: 'users-grid-row',
  nameCell: 'users-cell-name',
  statusFilter: 'users-filter-status',
  statusOption: 'users-filter-status-option',
} as const;

// Bounded wait so a RED spec (the Status multi-select control not yet wired) fails as a fast thrown
// assertion the test.fail() marker can absorb — never a 30s whole-test timeout (see carryover:
// "test.fail() absorbs assertions but not a whole-test timeout").
const FILTER_VISIBLE_TIMEOUT_MS = 5000;

export class UsersGridStatusFilterStatements {
  constructor(private readonly page: Page) {}

  async assertStatusFilterIsVisible(): Promise<void> {
    await expect(
      this.statusFilter(),
      'the Status column multi-select filter control is visible in the grid',
    ).toBeVisible({
      timeout: FILTER_VISIBLE_TIMEOUT_MS,
    });
  }

  async openStatusFilter(): Promise<void> {
    await this.statusFilter().click();
  }

  async assertStatusOptionsInLifecycleOrder(): Promise<void> {
    await expect(
      this.statusOptions(),
      'the status options are listed in lifecycle order (Pending, Active, Locked, Inactive), not alphabetically',
    ).toHaveText([...STATUSES_IN_LIFECYCLE_ORDER]);
  }

  async selectStatuses(): Promise<void> {
    for (const status of STATUS_FILTER_SELECTION) {
      await this.statusOption(status).click();
    }
  }

  async assertOnlyRowsWithSelectedStatusesRemain(): Promise<void> {
    await expect(
      this.rows(),
      'only rows whose status is one of the selected statuses (Pending, Locked) remain visible',
    ).toHaveCount(FULL_NAMES_MATCHING_STATUS_FILTER.length);
    await expect(
      this.nameCells(),
      'the surviving rows are exactly the Pending + Locked users, in original render order',
    ).toHaveText([...FULL_NAMES_MATCHING_STATUS_FILTER]);
  }

  private statusFilter(): Locator {
    return this.page.getByTestId(TEST_ID.statusFilter);
  }

  private statusOptions(): Locator {
    return this.page.getByTestId(TEST_ID.statusOption);
  }

  private statusOption(label: string): Locator {
    return this.statusOptions().filter({ hasText: label });
  }

  private nameCells(): Locator {
    return this.grid().getByTestId(TEST_ID.nameCell);
  }

  private rows(): Locator {
    return this.grid().getByTestId(TEST_ID.row);
  }

  private grid(): Locator {
    return this.page.getByTestId(TEST_ID.grid);
  }
}
