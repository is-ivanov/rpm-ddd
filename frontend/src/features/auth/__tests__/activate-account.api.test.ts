import { afterEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { issue } from 'allure-js-commons';
import { server } from '@/test/msw-server';
import { captureRejection } from '@/test/capture-rejection';
import { activateAccount } from '../logic/activation.api';
import { ActivationError } from '../logic/types';

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

function stubActivateRejectsWeakPassword(): void {
  server.use(
    http.post(`${BASE}/api/auth/activate`, () =>
      HttpResponse.json(
        {
          status: 422,
          title: 'Unprocessable Content',
          detail: 'Password does not meet the complexity requirements.',
          instance: '/api/auth/activate',
        },
        { status: 422, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

describe('Activate Account API Client', () => {
  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('reads the XSRF token cookie and posts token+password to activate the account', async () => {
    const captured: CapturedRequest = {};
    stubCsrfSetsCookie();
    stubActivateCapturing(captured);

    await activateAccount('valid-jwt-token', 'Str0ng-P@ssw0rd!');

    expect(captured.method).toBe('POST');
    expect(captured.pathname).toBe('/api/auth/activate');
    expect(captured.csrfHeader).toBe(XSRF_TOKEN);
    expect(captured.body).toEqual({ token: 'valid-jwt-token', password: 'Str0ng-P@ssw0rd!' });
  });

  // RED (#188) — activateAccount discards the Response and never checks response.ok, so it
  // resolves on a 422 instead of throwing. captureRejection then throws
  // Error('call resolved but should have rejected on an error status') before the assertions.
  it.fails('rejects with an ActivationError carrying the problem detail when the POST returns 422', async () => {
    await issue('188');
    stubCsrfSetsCookie();
    stubActivateRejectsWeakPassword();

    const error = await captureRejection(activateAccount('valid-jwt-token', 'weak'));

    expect(error).toBeInstanceOf(ActivationError);
    expect((error as ActivationError).message).toBe('Password does not meet the complexity requirements.');
  });
});
