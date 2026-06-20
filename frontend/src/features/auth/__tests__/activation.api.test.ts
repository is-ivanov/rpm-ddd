import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import { captureRejection } from '@/test/capture-rejection';
import { validateActivationToken } from '../logic/activation.api';
import { ActivationError } from '../logic/types';
import type { ActivationTokenResponse } from '../logic/types';

const BASE = import.meta.env.VITE_API_URL;

function stubActivateSuccess(captured: { url?: string }): void {
  server.use(
    http.get(`${BASE}/api/auth/activate`, ({ request }) => {
      captured.url = request.url;
      return HttpResponse.json({ login: 'iivanov', email: 'ivan@example.com' }, { status: 200 });
    }),
  );
}

function stubActivateMalformedSuccess(): void {
  server.use(http.get(`${BASE}/api/auth/activate`, () => HttpResponse.json({ login: 'iivanov' }, { status: 200 })));
}

function stubActivateExpired(): void {
  server.use(
    http.get(`${BASE}/api/auth/activate`, () =>
      HttpResponse.json(
        {
          status: 422,
          title: 'Unprocessable Content',
          detail: 'Token expired',
          instance: '/api/auth/activate',
        },
        { status: 422, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

describe('Activation API Client', () => {
  it('returns the login and email for a valid activation token', async () => {
    const captured: { url?: string } = {};
    stubActivateSuccess(captured);

    const result = await validateActivationToken('valid-jwt-token');

    const expected: ActivationTokenResponse = { login: 'iivanov', email: 'ivan@example.com' };
    expect(result).toEqual(expected);

    const requestUrl = new URL(captured.url ?? '');
    expect(requestUrl.pathname).toBe('/api/auth/activate');
    expect(requestUrl.searchParams.get('token')).toBe('valid-jwt-token');
  });

  // RED — validateActivationToken does a blind `as` cast (no zod parse), so a malformed
  // 200 body resolves instead of rejecting. Green wires schema.parse to reject it.
  it.fails('rejects when a 200 response body does not conform to the activation contract', async () => {
    stubActivateMalformedSuccess();

    const error = await captureRejection(validateActivationToken('valid-jwt-token'));

    expect(error).toBeInstanceOf(Error);
  });

  it('rejects with an ActivationError when the token is expired (422)', async () => {
    stubActivateExpired();

    const error = await captureRejection(validateActivationToken('expired-token'));

    expect(error).toBeInstanceOf(ActivationError);
    expect((error as ActivationError).message).toBe('Token expired');
  });
});
