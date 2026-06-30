import { expect, type Locator, type Page } from '@playwright/test';
import {
  EXPECTED_RELATIVE_LABEL,
  EXPECTED_TOOLTIP_DATE_FRAGMENT,
  EXPECTED_TOOLTIP_TZ_LABEL,
  EXPECTED_TOOLTIP_TIME_FRAGMENT,
  RELATIVE_TIME_ROW_INDEX,
  VIEWER_TIME_ZONE_ID,
} from '../support/users-grid-time.fixture';

const TEST_ID = {
  grid: 'users-grid',
  row: 'users-grid-row',
  createdCell: 'users-cell-created',
  tooltip: 'users-created-tooltip',
  tooltipDate: 'tooltip-date',
  tooltipTime: 'tooltip-time',
  tooltipTzLabel: 'tooltip-tz-label',
  tooltipTzId: 'tooltip-tz-id',
} as const;

export class UsersGridTimeStatements {
  constructor(private readonly page: Page) {}

  async hoverOverCreatedCell(rowIndex: number): Promise<void> {
    await this.createdCell(rowIndex).hover();
  }

  async assertCreatedCellShowsRelativeTime(expected: string): Promise<void> {
    await expect(
      this.createdCell(RELATIVE_TIME_ROW_INDEX),
      `Created cell at row ${RELATIVE_TIME_ROW_INDEX} shows the relative-time label, not the raw ISO timestamp`,
    ).toHaveText(expected);
  }

  async assertTooltipIsVisible(): Promise<void> {
    await expect(this.tooltip(), 'the absolute-time tooltip is revealed on hover').toBeVisible();
  }

  async assertTooltipShowsDate(fragment: string): Promise<void> {
    await expect(this.tooltipDate(), `tooltip date part contains "${fragment}"`).toContainText(fragment);
  }

  async assertTooltipShowsTime(fragment: string): Promise<void> {
    await expect(this.tooltipTime(), `tooltip time part contains "${fragment}"`).toContainText(fragment);
  }

  async assertTooltipShowsTimeZoneLabel(label: string): Promise<void> {
    await expect(this.tooltipTzLabel(), `tooltip timezone label is exactly "${label}"`).toHaveText(label);
  }

  async assertTooltipShowsIanaZoneId(zoneId: string): Promise<void> {
    await expect(this.tooltipTzId(), `tooltip IANA zone id is exactly "${zoneId}"`).toHaveText(zoneId);
  }

  async assertCreatedCellRelativeLabelAndTooltip(): Promise<void> {
    await this.assertCreatedCellShowsRelativeTime(EXPECTED_RELATIVE_LABEL);
    await this.hoverOverCreatedCell(RELATIVE_TIME_ROW_INDEX);
    await this.assertTooltipIsVisible();
    await this.assertTooltipShowsDate(EXPECTED_TOOLTIP_DATE_FRAGMENT);
    await this.assertTooltipShowsTime(EXPECTED_TOOLTIP_TIME_FRAGMENT);
    await this.assertTooltipShowsTimeZoneLabel(EXPECTED_TOOLTIP_TZ_LABEL);
    await this.assertTooltipShowsIanaZoneId(VIEWER_TIME_ZONE_ID);
  }

  private createdCell(rowIndex: number): Locator {
    return this.rows().nth(rowIndex).getByTestId(TEST_ID.createdCell);
  }

  private rows(): Locator {
    return this.grid().getByTestId(TEST_ID.row);
  }

  private tooltip(): Locator {
    return this.page.getByTestId(TEST_ID.tooltip);
  }

  private tooltipDate(): Locator {
    return this.tooltip().getByTestId(TEST_ID.tooltipDate);
  }

  private tooltipTime(): Locator {
    return this.tooltip().getByTestId(TEST_ID.tooltipTime);
  }

  private tooltipTzLabel(): Locator {
    return this.tooltip().getByTestId(TEST_ID.tooltipTzLabel);
  }

  private tooltipTzId(): Locator {
    return this.tooltip().getByTestId(TEST_ID.tooltipTzId);
  }

  private grid(): Locator {
    return this.page.getByTestId(TEST_ID.grid);
  }
}
