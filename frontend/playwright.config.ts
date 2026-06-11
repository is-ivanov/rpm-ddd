import { defineConfig, devices } from '@playwright/test';

const frontendPort = Number(process.env.FRONTEND_PORT) || 5173;
const appUrl = process.env.APP_URL || `http://localhost:${frontendPort}`;

export default defineConfig({
  testDir: './acceptance',
  testMatch: '**/*.spec.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: [
    ['list'],
    [
      'allure-playwright',
      {
        resultsDir: '../target/allure-results',
        links: {
          issue: {
            urlTemplate: 'https://github.com/is-ivanov/rpm-ddd/issues/%s',
            nameTemplate: 'Issue #%s',
          },
        },
      },
    ],
  ],
  use: {
    baseURL: appUrl,
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      testIgnore: '**/*.fullstack.spec.ts',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      // Full-stack E2E journey tier: real backend + Postgres + Mailpit, no
      // page.route mocking. Run only via `npm run test:e2e:fullstack`
      // (nightly), never in the default `test:e2e`. Retries the whole journey
      // because a mid-journey failure masks later steps.
      name: 'fullstack',
      testMatch: '**/*.fullstack.spec.ts',
      retries: 2,
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: appUrl,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: {
      FRONTEND_PORT: String(frontendPort),
    },
  },
});
