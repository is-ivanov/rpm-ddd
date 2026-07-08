import { expect, type Locator, type Page } from '@playwright/test';
import { EXPECTED_USER_ROWS, NO_MATCH_FULL_NAME_FILTER_TERM } from '../support/admin-users-fixture';
import { UsersGridLocators } from '../support/users-grid-locators';

const TEST_ID = {
  emptyMessage: 'users-grid-empty',
} as const;

// The exact empty-result message. There is no mockup for this state, so the test DEFINES the copy
// (a "you define it" value → strict equality) and align-design must render it verbatim.
const EMPTY_RESULT_MESSAGE = 'No users match your filters.';

// Bounded wait so a RED spec (the empty-result message not yet rendered) fails as a fast thrown
// assertion the test.fail() marker can absorb — never a 30s whole-test timeout (see carryover:
// "test.fail() absorbs assertions but not a whole-test timeout").
const EMPTY_MESSAGE_VISIBLE_TIMEOUT_MS = 5000;

// The full names of every seeded user, in render order — the exact set restored when the filter is
// cleared. Read straight off the fixture's declared `name` field (NOT re-run through the filter under
// test), so a buggy filter can never produce a matching-buggy expectation.
const ALL_FULL_NAMES: readonly string[] = EXPECTED_USER_ROWS.map((row) => row.name);

export class UsersGridEmptyResultStatements {
  private readonly grid: UsersGridLocators;

  constructor(private readonly page: Page) {
    this.grid = new UsersGridLocators(page);
  }

  async enterNoMatchFilter(): Promise<void> {
    await this.grid.nameFilter().fill(NO_MATCH_FULL_NAME_FILTER_TERM);
  }

  async clearFilter(): Promise<void> {
    await this.grid.nameFilter().fill('');
  }

  async assertEmptyResultMessageIsVisible(): Promise<void> {
    await expect(
      this.emptyMessage(),
      'the grid shows an empty-result message when the filter matches no rows',
    ).toBeVisible({ timeout: EMPTY_MESSAGE_VISIBLE_TIMEOUT_MS });
    await expect(this.emptyMessage(), 'the empty-result message text is exactly the defined copy').toHaveText(
      EMPTY_RESULT_MESSAGE,
    );
    await expect(this.grid.rows(), 'no user rows render while the filter matches nothing').toHaveCount(0);
  }

  async assertAllRowsRestored(): Promise<void> {
    await expect(this.grid.rows(), 'clearing the filter restores every user row').toHaveCount(ALL_FULL_NAMES.length);
    await expect(
      this.grid.nameCells(),
      'the restored rows are exactly every seeded user in the original render order',
    ).toHaveText([...ALL_FULL_NAMES]);
    await expect(this.emptyMessage(), 'the empty-result message is hidden once rows are restored').toHaveCount(0);
  }

  private emptyMessage(): Locator {
    return this.page.getByTestId(TEST_ID.emptyMessage);
  }
}
