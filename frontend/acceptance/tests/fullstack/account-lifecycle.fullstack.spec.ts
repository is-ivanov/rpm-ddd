import {test} from '@playwright/test';
import {LoginPageStatements} from '../statements/frontend/login-page.statements';
import {ActivationPageStatements} from '../statements/frontend/activation-page.statements';
import {type CreatedUser, RealAuthBackendStatements} from '../statements/backend/real-auth-backend.statements';
import {MailpitStatements} from '../statements/backend/mailpit.statements';

const ADMIN_LOGIN = 'admin';
const ADMIN_PASSWORD = 'admin';

test.describe('Account Lifecycle Full-Stack E2E (real backend + Postgres + Mailpit)', () => {
  let loginPage: LoginPageStatements;
  let activationPage: ActivationPageStatements;
  let realAuthBackend: RealAuthBackendStatements;
  let mailpit: MailpitStatements;

  test.beforeEach(({ page, baseURL }) => {
    loginPage = new LoginPageStatements(page, baseURL!);
    activationPage = new ActivationPageStatements(page, baseURL!);
    realAuthBackend = new RealAuthBackendStatements(page);
    mailpit = new MailpitStatements();
  });

  test(
    'Full-stack account lifecycle against the real stack - ' +
      'Given the pre-seeded ACTIVE admin exists in the real backend, ' +
      'When the admin logs in via the UI and creates a new user via the admin API, ' +
      'Then the new user receives an activation email in Mailpit, ' +
      'And the new user activates the account via the activation link and sets a password, ' +
      'And the new user logs in via the UI with the new credentials',
    async () => {
      // TDD RED: the skip marker stays until green-playwright (Step 5) runs the journey against the
      // live harness and removes it. RED is pinned by the Statements assertions: exact 201 on
      // admin create-user, the activation link present in the Mailpit email, the activation success
      // screen, and the JSESSIONID session cookie set after each UI login. Validated against the
      // real stack during RED: the journey passes once the async login side effect is awaited via a
      // polling cookie wait (the login page exposes no post-success UI signal — see improvements.md).
      test.skip();

      const newUser: CreatedUser = realAuthBackend.uniqueUserIdentity();

      await adminLogsInViaUi();
      await realAuthBackend.createUserAsAdmin(newUser);

      const activationToken = await mailpit.readActivationTokenFor(newUser.email);

      await newUserActivatesViaUi(activationToken, newUser.password);
      await newUserLogsInViaUi(newUser);
    },
  );

  async function adminLogsInViaUi(): Promise<void> {
    await loginPage.navigateToLoginPage();
    await loginPage.enterLoginText(ADMIN_LOGIN);
    await loginPage.enterPasswordText(ADMIN_PASSWORD);
    await loginPage.clickSubmitButton();
    await loginPage.assertErrorBannerIsAbsent();
    await realAuthBackend.assertSessionCookieIsSet();
  }

  async function newUserActivatesViaUi(activationToken: string, password: string): Promise<void> {
    // The activation token is read from the real activation email in Mailpit; opening the
    // activation page with it is the external entry point the user genuinely arrives at.
    await activationPage.navigateToActivationPageWithToken(activationToken);
    await activationPage.enterPassword(password);
    await activationPage.enterConfirmPassword(password);
    await activationPage.clickActivateButton();
    await activationPage.assertSuccessIconIsVisible();
    await activationPage.assertSuccessMessageIsDisplayed();
    await activationPage.clickGoToSignInButton();
    await activationPage.assertNavigatedToLoginPage();
  }

  async function newUserLogsInViaUi(newUser: CreatedUser): Promise<void> {
    await loginPage.enterLoginText(newUser.login);
    await loginPage.enterPasswordText(newUser.password);
    await loginPage.clickSubmitButton();
    await loginPage.assertErrorBannerIsAbsent();
    await realAuthBackend.assertSessionCookieIsSet();
  }
});
