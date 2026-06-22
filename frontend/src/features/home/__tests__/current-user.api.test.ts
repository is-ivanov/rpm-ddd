import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import { fetchCurrentUser } from '../logic/current-user.api';
import type { CurrentUserResult } from '../logic/types';

const BASE = import.meta.env.VITE_API_URL;

const ME_PATH = '/api/auth/me';

function stubMeUnauthenticated(): void {
  server.use(
    http.get(`${BASE}${ME_PATH}`, () =>
      HttpResponse.json(
        {
          type: 'https://www.rpm-ddd.my/problem/authentication-failed',
          title: 'Unauthorized',
          status: 401,
          detail: 'Full authentication is required to access this resource.',
          instance: ME_PATH,
        },
        { status: 401, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

describe('Current User API Client', () => {
  it('surfaces an unauthenticated result when GET /api/auth/me returns 401', async () => {
    stubMeUnauthenticated();

    const result = await fetchCurrentUser().catch((error: unknown) => error);

    const expected: CurrentUserResult = { authenticated: false };
    expect(result).toEqual(expected);
  });
});
