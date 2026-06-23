import { type Route } from '@playwright/test';

export const XSRF_TOKEN = 'test-xsrf-token';

/** Fulfills the CSRF handshake (GET /api/auth/csrf) by setting the XSRF-TOKEN cookie. */
export async function fulfillCsrfRoute(route: Route): Promise<void> {
  await route.fulfill({
    status: 200,
    contentType: 'application/json',
    headers: { 'Set-Cookie': `XSRF-TOKEN=${XSRF_TOKEN}; Path=/` },
    body: '{}',
  });
}
