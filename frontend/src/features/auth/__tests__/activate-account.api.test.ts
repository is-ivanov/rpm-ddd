import { afterEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import { activateAccount } from '../logic/activation.api';

const BASE = import.meta.env.VITE_API_URL;

const XSRF_TOKEN = 'test-xsrf-token';

interface CapturedRequest {
  method?: string;
  pathname?: string;
  csrfHeader?: string | null;
  body?: unknown;
}

function stubCsrfSetsCookie(): void {
  server.use(
    http.get(`${BASE}/api/auth/csrf`, () => {
      document.cookie = `XSRF-TOKEN=${XSRF_TOKEN}; Path=/`;
      return HttpResponse.json({}, { status: 200 });
    }),
  );
}

function stubActivateCapturing(captured: CapturedRequest): void {
  server.use(
    http.post(`${BASE}/api/auth/activate`, async ({ request }) => {
      captured.method = request.method;
      captured.pathname = new URL(request.url).pathname;
      captured.csrfHeader = request.headers.get('X-XSRF-TOKEN');
      captured.body = await request.json();
      return HttpResponse.json({}, { status: 200 });
    }),
  );
}

describe('Activate Account API Client', () => {
  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  // .skip: activateAccount is a not-implemented stub (throws). green-frontend-api wires
  // GET /api/auth/csrf -> read XSRF-TOKEN cookie -> POST /api/auth/activate with header.
  it.skip('reads the XSRF token cookie and posts token+password to activate the account', async () => {
    const captured: CapturedRequest = {};
    stubCsrfSetsCookie();
    stubActivateCapturing(captured);

    await activateAccount('valid-jwt-token', 'Str0ng-P@ssw0rd!');

    expect(captured.method).toBe('POST');
    expect(captured.pathname).toBe('/api/auth/activate');
    expect(captured.csrfHeader).toBe(XSRF_TOKEN);
    expect(captured.body).toEqual({ token: 'valid-jwt-token', password: 'Str0ng-P@ssw0rd!' });
  });
});
