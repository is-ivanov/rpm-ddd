import { expect, type Locator, type Page } from '@playwright/test';
import {
  FULL_NAMES_MATCHING_LOGIN_AND_UPDATED_BY,
  LOGIN_FILTER_TERM,
  UPDATED_BY_FILTER_TERM,
} from '../support/admin-users-fixture';
import { UsersGridLocators } from '../support/users-grid-locators';

const TEST_ID = {
  loginFilter: 'users-filter-login',
  updatedByFilter: 'users-filter-updated-by',
} as const;

// Bounded wait so a RED spec (filter input not yet wired) fails as a fast thrown assertion the
// test.fail() marker can absorb — never a 30s whole-test timeout (see carryover: "test.fail()
// absorbs assertions but not a whole-test timeout").
const FILTER_VISIBLE_TIMEOUT_MS = 5000;

export class UsersGridFilterStatements {
  private readonly grid: UsersGridLocators;

  constructor(private readonly page: Page) {
    this.grid = new UsersGridLocators(page);
  }

  async assertLoginFilterIsVisible(): Promise<void> {
    await expect(this.loginFilter(), 'the Login column filter input is visible in the grid').toBeVisible({
      timeout: FILTER_VISIBLE_TIMEOUT_MS,
    });
  }

  async assertUpdatedByFilterIsVisible(): Promise<void> {
    await expect(this.updatedByFilter(), 'the Updated-by column filter input is visible in the grid').toBeVisible({
      timeout: FILTER_VISIBLE_TIMEOUT_MS,
    });
  }

  async enterLoginFilter(): Promise<void> {
    await this.loginFilter().fill(LOGIN_FILTER_TERM);
  }

  async enterUpdatedByFilter(): Promise<void> {
    await this.updatedByFilter().fill(UPDATED_BY_FILTER_TERM);
  }

  async assertOnlyRowsMatchingBothFiltersRemain(): Promise<void> {
    await expect(
      this.grid.rows(),
      'only rows matching BOTH column filters remain visible (AND-combined, not per-column replacement)',
    ).toHaveCount(FULL_NAMES_MATCHING_LOGIN_AND_UPDATED_BY.length);
    await expect(
      this.grid.nameCells(),
      'the sole surviving row is the one contained in both filters (David Lee)',
    ).toHaveText([...FULL_NAMES_MATCHING_LOGIN_AND_UPDATED_BY]);
  }

  private loginFilter(): Locator {
    return this.page.getByTestId(TEST_ID.loginFilter);
  }

  private updatedByFilter(): Locator {
    return this.page.getByTestId(TEST_ID.updatedByFilter);
  }
}
