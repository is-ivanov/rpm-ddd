import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';
import { CurrentUserBackendStatements } from '../../statements/backend/current-user-backend.statements';
import { AppInfoBackendStatements } from '../../statements/backend/app-info-backend.statements';
import { AppVersionStatements } from '../../statements/frontend/app-version.statements';

const VERSION = '1.4.2';
const COMMIT = 'abc1234';
const BUILD_TIME = '2026-06-20T10:20:00Z';

test.describe('App Version Popover', () => {
  let homePage: HomePageStatements;
  let currentUserBackend: CurrentUserBackendStatements;
  let appInfoBackend: AppInfoBackendStatements;
  let appVersion: AppVersionStatements;

  test.beforeEach(({ page, baseURL }) => {
    homePage = new HomePageStatements(page, baseURL);
    currentUserBackend = new CurrentUserBackendStatements(page);
    appInfoBackend = new AppInfoBackendStatements(page);
    appVersion = new AppVersionStatements(page);
  });

  test(
    'UI Task #215: Header version popover shows the deployed app version - ' +
      'Given an authenticated user, ' +
      'And the deployed app reports version "1.4.2", commit "abc1234", and a build time, ' +
      'When the user clicks the help icon in the top bar, ' +
      'Then a popover displays the version "1.4.2", ' +
      'And the popover displays the commit "abc1234", ' +
      'And the popover displays the build time',
    async () => {
      await issue('215');
      // RED: the header help icon and version popover (app-version-* test-ids) do not exist yet;
      // the AppVersion component is built later (red-frontend / align-design / green-frontend).
      // assertHelpIconIsVisible / clickHelpIcon will time out waiting for the missing app-version-trigger.
      test.fail();

      await currentUserBackend.givenAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });
      await appInfoBackend.givenAppInfo({ version: VERSION, commit: COMMIT, buildTime: BUILD_TIME });
      await homePage.navigateToHomePage();

      await homePage.assertDashboardShellIsVisible();
      await appVersion.assertHelpIconIsVisible();
      await appVersion.clickHelpIcon();
      await appVersion.assertPopoverIsVisible();
      await appVersion.assertPopoverShowsVersion(VERSION);
      await appVersion.assertPopoverShowsCommit(COMMIT);
      await appVersion.assertBuildTimeIsVisible();
    },
  );
});
