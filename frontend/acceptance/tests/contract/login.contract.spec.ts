import { test } from '@playwright/test';
import { LoginPageStatements } from '../statements/frontend/login-page.statements';
import { RealAuthBackendStatements } from '../statements/backend/real-auth-backend.statements';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080';

test.describe('Login Contract (real backend)', () => {
  let loginPage: LoginPageStatements;
  let realAuthBackend: RealAuthBackendStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
    realAuthBackend = new RealAuthBackendStatements(page, BACKEND_URL);
  });

  test(
    'Contract: valid login against the real backend sets the session cookie - ' +
      'Given an active user exists in the real backend, ' +
      'And the user navigates to the login page, ' +
      'When the user enters valid credentials, ' +
      'And the user clicks the "Sign In" button, ' +
      'Then no error banner appears, ' +
      'And the JSESSIONID session cookie is set',
    async () => {
      // TDD RED: seeding fails — admin/admin login returns 401 against the production-migrations
      // backend (no admin user is seeded in prod). A real-API path to a loginable user requires the
      // admin-bootstrap / Postgres-seed decided by the Step 4 ADR and provisioned in Step 3.
      test.skip();

      await realAuthBackend.givenActiveUser({ login: 'contract_user', email: 'contract_user@localhost.com' });
      await loginPage.navigateToLoginPage();

      await loginPage.enterLoginText('contract_user');
      await loginPage.enterPasswordText('Contract@123');
      await loginPage.clickSubmitButton();

      await loginPage.assertErrorBannerIsAbsent();
      await realAuthBackend.assertSessionCookieIsSet();
    },
  );
});
