import { expect, type Locator, type Page } from '@playwright/test';
import { DUPLICATE_LOGIN_ERROR_MESSAGE, NEW_USER_INPUT } from '../support/register-user-fixture';

const TEST_ID = {
  modal: 'register-user-modal',
  submitButton: 'register-user-submit',
  submitSpinner: 'register-user-submit-spinner',
  cancelButton: 'register-user-cancel',
  timezoneControl: 'register-user-timezone',
  loginFieldError: 'register-user-login-error',
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

const VALID_INPUT_VALUES = [
  { control: 'register-user-first-name', value: NEW_USER_INPUT.firstName },
  { control: 'register-user-middle-name', value: NEW_USER_INPUT.middleName },
  { control: 'register-user-last-name', value: NEW_USER_INPUT.lastName },
  { control: 'register-user-login', value: NEW_USER_INPUT.login },
  { control: 'register-user-email', value: NEW_USER_INPUT.email },
] as const;

// A subset of fields the E2E types before cancelling (scenario 5.3): enough to prove the modal
// held partial input. On reopen every one of these must be empty again — the cancelled input is
// discarded because the modal unmounts (v-if) and remounts with a fresh, empty form.
const PARTIAL_INPUT_VALUES = [
  { control: 'register-user-first-name', value: NEW_USER_INPUT.firstName },
  { control: 'register-user-last-name', value: NEW_USER_INPUT.lastName },
  { control: 'register-user-login', value: NEW_USER_INPUT.login },
] as const;

export interface RegisterUserIdentity {
  readonly firstName: string;
  readonly middleName: string;
  readonly lastName: string;
  readonly login: string;
  readonly email: string;
}

export class RegisterUserModalStatements {
  constructor(private readonly page: Page) {}

  async assertModalIsOpen(): Promise<void> {
    await expect(this.modal(), 'the Register user modal is open').toBeVisible({ timeout: 5000 });
  }

  async assertModalIsClosed(): Promise<void> {
    await expect(this.modal(), 'the Register user modal closes after a successful create').toHaveCount(0, {
      timeout: 5000,
    });
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

  async fillWithValidValues(): Promise<void> {
    await this.fillFields(VALID_INPUT_VALUES);
  }

  // Fill the modal from a unique-per-run identity (full-stack journey), so retries
  // never collide on a duplicate login/email against the persistent Postgres.
  async fillFromIdentity(identity: RegisterUserIdentity): Promise<void> {
    await this.fillFields([
      { control: 'register-user-first-name', value: identity.firstName },
      { control: 'register-user-middle-name', value: identity.middleName },
      { control: 'register-user-last-name', value: identity.lastName },
      { control: 'register-user-login', value: identity.login },
      { control: 'register-user-email', value: identity.email },
    ]);
  }

  async clickRegister(): Promise<void> {
    await this.submitButton().click();
  }

  async fillPartialValues(): Promise<void> {
    await this.fillFields(PARTIAL_INPUT_VALUES);
  }

  async clickCancel(): Promise<void> {
    await this.cancelButton().click();
  }

  async pressEscape(): Promise<void> {
    await this.page.keyboard.press('Escape');
  }

  async assertFieldsAreDiscardedOnReopen(): Promise<void> {
    await expect(this.modal(), 'the Register user modal reopens for the discard check').toBeVisible({ timeout: 5000 });
    for (const field of PARTIAL_INPUT_VALUES) {
      await expect(
        this.page.getByTestId(field.control),
        `the "${field.control}" field is empty on reopen — the cancelled input was discarded`,
      ).toHaveValue('');
    }
  }

  async assertSubmitButtonShowsLoadingIndicator(): Promise<void> {
    await expect(
      this.submitSpinner(),
      'the "Register" button shows a loading indicator while the create request is in flight',
    ).toBeVisible({ timeout: 5000 });
  }

  async assertFormFieldsAreDisabled(): Promise<void> {
    for (const field of VALID_INPUT_VALUES) {
      await expect(
        this.page.getByTestId(field.control),
        `the "${field.control}" field is disabled during submission`,
      ).toBeDisabled();
    }
    await expect(this.submitButton(), 'the "Register" submit button is disabled during submission').toBeDisabled();
  }

  async assertLoginFieldErrorIsShown(): Promise<void> {
    await expect(
      this.loginFieldError(),
      'a field-level error is shown under the Login field after a duplicate-login rejection',
    ).toBeVisible({ timeout: 5000 });
    await expect(
      this.loginFieldError(),
      `the Login field error text is exactly "${DUPLICATE_LOGIN_ERROR_MESSAGE}"`,
    ).toHaveText(DUPLICATE_LOGIN_ERROR_MESSAGE);
  }

  async assertModalStaysOpen(): Promise<void> {
    await expect(this.modal(), 'the Register user modal stays open after the rejected submit').toBeVisible();
  }

  async assertEnteredValuesArePreserved(): Promise<void> {
    for (const field of VALID_INPUT_VALUES) {
      await expect(
        this.page.getByTestId(field.control),
        `the "${field.control}" field still holds the submitted value after the rejection`,
      ).toHaveValue(field.value);
    }
  }

  private async fillFields(fields: readonly { control: string; value: string }[]): Promise<void> {
    for (const field of fields) {
      await this.page.getByTestId(field.control).fill(field.value);
    }
  }

  private loginFieldError(): Locator {
    return this.page.getByTestId(TEST_ID.loginFieldError);
  }

  private modal(): Locator {
    return this.page.getByTestId(TEST_ID.modal);
  }

  private submitSpinner(): Locator {
    return this.page.getByTestId(TEST_ID.submitSpinner);
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
