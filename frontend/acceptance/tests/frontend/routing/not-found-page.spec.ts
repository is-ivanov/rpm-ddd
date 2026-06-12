import { test } from '@playwright/test';
import { issue } from 'allure-js-commons';
import { NotFoundPageStatements } from '../../statements/frontend/not-found-page.statements';
import { HomePageStatements } from '../../statements/frontend/home-page.statements';

test.describe('Not Found Page', () => {
  let notFoundPage: NotFoundPageStatements;
  let homePage: HomePageStatements;

  test.beforeEach(({ page, baseURL }) => {
    notFoundPage = new NotFoundPageStatements(page, baseURL!);
    homePage = new HomePageStatements(page);
  });

  // RED (#162): the router defines only home/login/activate with no catch-all route and no
  // NotFoundPage component exists — the unknown route matches nothing, router-view renders empty,
  // so assertNotFoundViewIsVisible (toBeVisible on getByTestId('not-found-page')) times out with
  // "element(s) not found".
  test.fail(
    'UI Bug #162: Navigating to an unknown client route renders the NotFound view with a link back to a known route - ' +
      'Given the SPA is served, ' +
      'When the user deep-links to an unknown client route, ' +
      'Then the NotFound view is displayed with a friendly 404 message, ' +
      'And the view contains a link back to the home page, ' +
      'And clicking the link lands the user on the home page',
    async () => {
      await issue('162');

      await notFoundPage.navigateToUnknownRoute();

      await notFoundPage.assertNotFoundViewIsVisible();
      await notFoundPage.assertNotFoundMessageIsVisible();
      await notFoundPage.assertBackToHomeLinkIsVisible();

      await notFoundPage.clickBackToHomeLink();

      await homePage.assertHomePageIsVisible();
    },
  );
});
