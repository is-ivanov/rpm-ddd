import { type Locator, type Page } from '@playwright/test';

// The Users grid's shared structural test-ids, declared ONCE. Every per-concern Statements class
// (filter, sort, time, error, status-filter, date-filter) and the UsersPage page object drives the
// SAME grid container, rows, and name cells; before this helper each of those files redeclared
// `grid`/`row`/`nameCell` and their own private grid()/rows()/nameCells() locators — the "shared
// per-page locators belong in ONE place" rule (frontend-rules.md → Playwright Tests). Concern-specific
// cells (login, status badge, created cell, filter inputs) stay in their own Statements, scoped off
// grid()/rows() here.
export const USERS_GRID_TEST_ID = {
  grid: 'users-grid',
  row: 'users-grid-row',
  nameCell: 'users-cell-name',
  loading: 'users-grid-loading',
} as const;

export class UsersGridLocators {
  constructor(private readonly page: Page) {}

  grid(): Locator {
    return this.page.getByTestId(USERS_GRID_TEST_ID.grid);
  }

  rows(): Locator {
    return this.grid().getByTestId(USERS_GRID_TEST_ID.row);
  }

  nameCells(): Locator {
    return this.grid().getByTestId(USERS_GRID_TEST_ID.nameCell);
  }

  // The grid's loading indicator — shown in place of the grid while the list request is in flight.
  // Not scoped under grid(): it renders instead of the grid, so it lives at the page level.
  loading(): Locator {
    return this.page.getByTestId(USERS_GRID_TEST_ID.loading);
  }
}
