import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  collapseToggle: 'sidebar-collapse-toggle',
  dashboardSidebar: 'dashboard-sidebar',
} as const;

const COLLAPSED_ATTRIBUTE = 'data-collapsed';
const TOGGLE_VISIBLE_TIMEOUT_MS = 5000;

export class SidebarCollapseStatements {
  constructor(private readonly page: Page) {}

  async assertCollapseToggleIsVisible(): Promise<void> {
    await expect(this.collapseToggle(), 'sidebar collapse toggle is visible in the top bar').toBeVisible({
      timeout: TOGGLE_VISIBLE_TIMEOUT_MS,
    });
  }

  async assertSidebarIsExpanded(): Promise<void> {
    await expect(this.sidebar(), 'sidebar is in the expanded state').toHaveAttribute(COLLAPSED_ATTRIBUTE, 'false');
  }

  async clickCollapseToggle(): Promise<void> {
    await this.collapseToggle().click();
  }

  async assertSidebarIsCollapsed(): Promise<void> {
    await expect(this.sidebar(), 'sidebar is in the collapsed state').toHaveAttribute(COLLAPSED_ATTRIBUTE, 'true');
  }

  async reloadPage(): Promise<void> {
    await this.page.reload();
  }

  private collapseToggle(): Locator {
    return this.page.getByTestId(TEST_ID.collapseToggle);
  }

  private sidebar(): Locator {
    return this.page.getByTestId(TEST_ID.dashboardSidebar);
  }
}
