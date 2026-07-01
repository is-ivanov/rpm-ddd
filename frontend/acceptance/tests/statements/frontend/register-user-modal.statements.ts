import { expect, type Locator, type Page } from '@playwright/test';

const TEST_ID = {
  modal: 'register-user-modal',
  submitButton: 'register-user-submit',
  cancelButton: 'register-user-cancel',
  timezoneControl: 'register-user-timezone',
} as const;

const SUBMIT_BUTTON_TEXT = 'Register';
const CANCEL_BUTTON_TEXT = 'Cancel';
const APP_DEFAULT_TIMEZONE_LABEL = '(UTC+01:00) Central European Time — Europe/Berlin';

const FIELDS = [
  { control: 'register-user-first-name', label: 'register-user-first-name-label', text: 'First name' },
  { control: 'register-user-middle-name', label: 'register-user-middle-name-label', text: 'Middle name' },
  { control: 'register-user-last-name', label: 'register-user-last-name-label', text: 'Last name' },
  { control: 'register-user-login', label: 'register-user-login-label', text: 'Login' },
  { control: 'register-user-email', label: 'register-user-email-label', text: 'Email' },
  { control: TEST_ID.timezoneControl, label: 'register-user-timezone-label', text: 'Timezone' },
] as const;

export class RegisterUserModalStatements {
  constructor(private readonly page: Page) {}

  async assertModalIsOpen(): Promise<void> {
    await expect(this.modal(), 'the Register user modal is open').toBeVisible({ timeout: 5000 });
  }

  async assertAllFieldsAreVisible(): Promise<void> {
    for (const field of FIELDS) {
      const label = this.page.getByTestId(field.label);
      await expect(this.page.getByTestId(field.control), `the "${field.text}" field control is visible`).toBeVisible();
      await expect(label, `the "${field.text}" field label is visible`).toBeVisible();
      await expect(label, `the field label reads "${field.text}"`).toContainText(field.text);
    }
  }

  async assertTimezonePrefilledWithAppDefault(): Promise<void> {
    await expect(
      this.timezoneControl(),
      `the Timezone field is pre-filled with the exact app default ("${APP_DEFAULT_TIMEZONE_LABEL}")`,
    ).toHaveText(APP_DEFAULT_TIMEZONE_LABEL);
  }

  async assertRegisterAndCancelButtonsAreVisible(): Promise<void> {
    await expect(this.submitButton(), 'the "Register" submit button is visible').toBeVisible();
    await expect(this.submitButton(), 'the submit button text is exactly "Register"').toHaveText(SUBMIT_BUTTON_TEXT);
    await expect(this.cancelButton(), 'the "Cancel" button is visible').toBeVisible();
    await expect(this.cancelButton(), 'the cancel button text is exactly "Cancel"').toHaveText(CANCEL_BUTTON_TEXT);
  }

  private modal(): Locator {
    return this.page.getByTestId(TEST_ID.modal);
  }

  private timezoneControl(): Locator {
    return this.page.getByTestId(TEST_ID.timezoneControl);
  }

  private submitButton(): Locator {
    return this.page.getByTestId(TEST_ID.submitButton);
  }

  private cancelButton(): Locator {
    return this.page.getByTestId(TEST_ID.cancelButton);
  }
}
