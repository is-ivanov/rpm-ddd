import { test } from '@playwright/test';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { UsersPageStatements } from '../../statements/frontend/users-page.statements';
import { RegisterUserModalStatements } from '../../statements/frontend/register-user-modal.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AdminUsersBackendStatements } from '../../statements/backend/admin-users-backend.statements';
import { CreateUserBackendStatements } from '../../statements/backend/create-user-backend.statements';

test.describe('Register User Modal', () => {
  let homePage: HomePageStatements;
  let usersPage: UsersPageStatements;
  let modal: RegisterUserModalStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let adminUsersBackend: AdminUsersBackendStatements;
  let createUserBackend: CreateUserBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    usersPage = new UsersPageStatements(page);
    modal = new RegisterUserModalStatements(page);
    currentUserBackend = new CurrentUserBackendStatements(page);
    adminUsersBackend = new AdminUsersBackendStatements(page);
    createUserBackend = new CreateUserBackendStatements(page);
  });

  test(
    'UI Test Scenario 4.1: Register user opens a modal with the timezone pre-filled - ' +
      'Given the user is on the Users page, ' +
      'When the user clicks the "Register user" button, ' +
      'Then a modal opens with fields: First name, Middle name, Last name, Login, Email, Timezone, ' +
      'And the Timezone field is pre-filled with the app default (Central Europe), ' +
      'And the modal shows a "Register" submit button and a "Cancel" button',
    async () => {
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
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
      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await adminUsersBackend.givenSeveralUsers();
      await createUserBackend.givenCreateUserInFlight();
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
});
