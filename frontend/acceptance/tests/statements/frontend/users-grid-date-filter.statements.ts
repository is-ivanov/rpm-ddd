import { expect, type Locator, type Page } from '@playwright/test';
import {
  CREATED_RANGE_FROM,
  CREATED_RANGE_TO,
  FULL_NAMES_IN_CREATED_RANGE,
} from '../support/admin-users-date-filter.fixture';
import { UsersGridLocators } from '../support/users-grid-locators';

const TEST_ID = {
  createdRangeFilter: 'users-filter-created-range',
  createdFrom: 'users-filter-created-from',
  createdTo: 'users-filter-created-to',
} as const;

// Bounded wait so a RED spec (the Created date-range control not yet wired) fails as a fast thrown
// assertion the test.fail() marker can absorb — never a 30s whole-test timeout (see carryover:
// "test.fail() absorbs assertions but not a whole-test timeout").
const FILTER_VISIBLE_TIMEOUT_MS = 5000;

export class UsersGridDateFilterStatements {
  private readonly grid: UsersGridLocators;

  constructor(private readonly page: Page) {
    this.grid = new UsersGridLocators(page);
  }

  async assertCreatedRangeFilterIsVisible(): Promise<void> {
    await expect(
      this.createdRangeFilter(),
      'the Created column from–to date-range filter control is visible in the grid',
    ).toBeVisible({
      timeout: FILTER_VISIBLE_TIMEOUT_MS,
    });
  }

  async openCreatedRangeFilter(): Promise<void> {
    await this.createdRangeFilter().click();
  }

  async enterCreatedRange(): Promise<void> {
    await this.createdFrom().fill(CREATED_RANGE_FROM);
    await this.createdTo().fill(CREATED_RANGE_TO);
  }

  async assertOnlyRowsInCreatedRangeRemain(): Promise<void> {
    await expect(
      this.grid.rows(),
      'only rows whose underlying created instant falls within the from–to range remain visible',
    ).toHaveCount(FULL_NAMES_IN_CREATED_RANGE.length);
    await expect(
      this.grid.nameCells(),
      'the surviving rows are exactly the users created within the range, in original render order',
    ).toHaveText([...FULL_NAMES_IN_CREATED_RANGE]);
  }

  private createdRangeFilter(): Locator {
    return this.page.getByTestId(TEST_ID.createdRangeFilter);
  }

  private createdFrom(): Locator {
    return this.page.getByTestId(TEST_ID.createdFrom);
  }

  private createdTo(): Locator {
    return this.page.getByTestId(TEST_ID.createdTo);
  }
}
