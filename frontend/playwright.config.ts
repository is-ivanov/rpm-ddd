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
