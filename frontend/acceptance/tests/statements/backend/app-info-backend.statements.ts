import { type Page, type Route } from '@playwright/test';

const APP_INFO_URL_PATTERN = '**/actuator/info';

const GIT_BRANCH = 'main';
const GIT_COMMIT_TIME = '2026-06-20T10:15:30Z';
const BUILD_ARTIFACT = 'rpm-ddd';
const BUILD_GROUP = 'by.iivanov.rpm';

interface AppInfo {
  readonly version: string;
  readonly commit: string;
  readonly buildTime: string;
}

export class AppInfoBackendStatements {
  constructor(private readonly page: Page) {}

  async givenAppInfo(info: AppInfo): Promise<void> {
    await this.page.route(APP_INFO_URL_PATTERN, (route) => this.fulfillAppInfo(route, info));
  }

  private async fulfillAppInfo(route: Route, info: AppInfo): Promise<void> {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        git: {
          branch: GIT_BRANCH,
          commit: { id: info.commit, time: GIT_COMMIT_TIME },
        },
        build: {
          artifact: BUILD_ARTIFACT,
          name: BUILD_ARTIFACT,
          version: info.version,
          group: BUILD_GROUP,
          time: info.buildTime,
        },
      }),
    });
  }
}
