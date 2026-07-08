import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { RegisterUserModalStatements } from '../../statements/frontend/register-user-modal.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';
import { RegisterUserBackendStatements } from '../../statements/backend/register-user-backend.statements';

test.describe('Register User Modal', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let modal: RegisterUserModalStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;
  let registerUserBackend: RegisterUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    modal = new RegisterUserModalStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
    registerUserBackend = new RegisterUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.1: Register user opens a modal with the timezone pre-filled - ' +
      'Given the user is on the Users page, ' +
      'When the user clicks the "Register user" button, ' +
      'Then a modal opens with fields: First name, Middle name, Last name, Login, Email, Timezone, ' +
      'And the Timezone field is pre-filled with the app default (Central Europe), ' +
      'And the modal shows a "Register" submit button and a "Cancel" button',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.assertRegisterUserButtonIsVisible();

      await usersPage.clickRegisterUserButton();

      await modal.assertModalIsOpen();
      await modal.assertAllFieldsAreVisible();
      await modal.assertTimezonePrefilledWithAppDefault();
      await modal.assertRegisterAndCancelButtonsAreVisible();
    },
  );

  test(
    'UI Test Scenario 4.2: Modal shows a loading state during submission - ' +
      'Given the Register user modal is filled with valid values, ' +
      'When the user clicks "Register", ' +
      'Then the Register button shows a loading indicator, ' +
      'And the form fields become disabled during submission',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await registerUserBackend.givenRegisterUserInFlight();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.clickRegisterUserButton();
      await modal.assertModalIsOpen();
      await modal.fillWithValidValues();

      await modal.clickRegister();

      await modal.assertSubmitButtonShowsLoadingIndicator();
      await modal.assertFormFieldsAreDisabled();
    },
  );

  test(
    'UI Test Scenario 5.1: Successful create closes the modal and refreshes the grid - ' +
      'Given the Register user modal is filled with valid values, ' +
      'When the user clicks "Register" and the request succeeds, ' +
      'Then the modal closes, ' +
      'And the grid refreshes and shows the newly created user with status Pending',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenListRefreshesWithNewUserAfterCreate();
      await registerUserBackend.givenRegisterUserSucceeds();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.clickRegisterUserButton();
      await modal.assertModalIsOpen();
      await modal.fillWithValidValues();

      await modal.clickRegister();

      await modal.assertModalIsClosed();
      await usersPage.assertNewPendingUserRowIsVisible();
    },
  );

  test(
    'UI Test Scenario 5.2: Duplicate login or email shows a field-level error - ' +
      'Given the Register user modal is filled with a login that already exists, ' +
      'When the user clicks "Register" and the server returns a duplicate-login error, ' +
      'Then a field-level error message is shown under the Login field, ' +
      'And the modal stays open with the entered values preserved',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await registerUserBackend.givenRegisterUserRejectsDuplicateLogin();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.clickRegisterUserButton();
      await modal.assertModalIsOpen();
      await modal.fillWithValidValues();

      await modal.clickRegister();

      await modal.assertLoginFieldErrorIsShown();
      await modal.assertModalStaysOpen();
      await modal.assertEnteredValuesArePreserved();
    },
  );

  test(
    'UI Test Scenario 5.3: Cancelling the register modal discards input and leaves the grid unchanged - ' +
      'Given the Register user modal is open with partially entered values, ' +
      'When the user clicks "Cancel", ' +
      'Then the modal closes, ' +
      'And no new row is added to the grid, ' +
      'And the entered input is discarded',
    async () => {
      await currentUserBackend.givenAuthenticatedUser();
      await adminUsersBackend.givenSeveralUsers();
      await homePage.navigateToHomePage();
      await homePage.clickUsersNavItem();
      await usersPage.clickRegisterUserButton();
      await modal.assertModalIsOpen();
      await modal.fillPartialValues();

      await modal.clickCancel();

      await modal.assertModalIsClosed();
      await usersPage.assertGridRowCountUnchanged();
      await usersPage.clickRegisterUserButton();
      await modal.assertFieldsAreDiscardedOnReopen();
    },
  );
});
