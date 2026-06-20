import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  successScreen: 'activation-success',
  serverError: 'activation-server-error',
} as const;

export class ActivationFailureStatements {
  constructor(private readonly page: Page) {}

  async assertSuccessScreenIsNotVisible(): Promise<void> {
    await expect(
      this.successScreen(),
      'success screen must NOT be shown when the backend rejects the activation (4xx)',
    ).toBeHidden();
  }

  async assertServerErrorIsDisplayed(expectedDetail: string): Promise<void> {
    await expect(this.serverError(), 'a server error message is displayed when activation is rejected').toBeVisible();
    await expect(this.serverError(), 'the server error shows the backend ProblemDetail message').toHaveText(
      expectedDetail,
    );
  }

  private successScreen(): Locator {
    return this.page.getByTestId(TEST_ID.successScreen);
  }

  private serverError(): Locator {
    return this.page.getByTestId(TEST_ID.serverError);
  }
}
