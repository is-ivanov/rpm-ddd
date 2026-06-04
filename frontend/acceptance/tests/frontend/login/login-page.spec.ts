import { test } from '@playwright/test';
import { LoginPageStatements } from '../../statements/frontend/login-page.statements';

test.describe('Login Page', () => {
  let loginPage: LoginPageStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
  });

  test('UI Test Scenario 1.1: Login page shows login and password fields and submit button - Given the user navigates to the login page, Then the page displays a login input field, And the page displays a password input field, And the password field masks entered text, And the page displays a submit button with text "Sign In"', async () => {
    await loginPage.navigateToLoginPage();

    await loginPage.assertLoginFieldIsVisible();
    await loginPage.assertPasswordFieldIsVisible();
    await loginPage.assertPasswordFieldMasksText();
    await loginPage.assertSubmitButtonIsVisible();
  });
});
