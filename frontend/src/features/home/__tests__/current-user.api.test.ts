import { describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { server } from '@/test/msw-server';
import { fetchCurrentUser } from '../logic/current-user.api';
import type { CurrentUserResult } from '../logic/types';

const BASE = import.meta.env.VITE_API_URL;

const ME_PATH = '/api/auth/me';

function stubMe(body: JsonBodyType, init: ResponseInit): void {
  server.use(http.get(`${BASE}${ME_PATH}`, () => HttpResponse.json(body, init)));
}

function stubMeUnauthenticated(): void {
  stubMe(
    {
      type: 'https://www.rpm-ddd.my/problem/authentication-failed',
      title: 'Unauthorized',
      status: 401,
      detail: 'Full authentication is required to access this resource.',
      instance: ME_PATH,
    },
    { status: 401, headers: { 'Content-Type': 'application/problem+json' } },
  );
}

function stubMeAuthenticated(): void {
  stubMe(
    {
      userId: '11111111-1111-1111-1111-111111111111',
      login: 'ipetrov',
      email: 'i.petrov@rpm.local',
      firstName: 'Иван',
      lastName: 'Петров',
      status: 'ACTIVE',
      roles: [],
    },
    { status: 200 },
  );
}

describe('Current User API Client', () => {
  it('surfaces an unauthenticated result when GET /api/auth/me returns 401', async () => {
    stubMeUnauthenticated();

    const result = await fetchCurrentUser().catch((error: unknown) => error);

    const expected: CurrentUserResult = { authenticated: false };
    expect(result).toEqual(expected);
  });

  // RED: current-user.api throws 'Authenticated user mapping not implemented yet.'
  // for the 200 path, so the mapped CurrentUserResult does not yet exist. Remove
  // it.fails once green-frontend-api maps the authenticated payload.
  it.fails('maps an authenticated result when GET /api/auth/me returns 200', async () => {
    stubMeAuthenticated();

    const result = await fetchCurrentUser().catch((error: unknown) => error);

    const expected: CurrentUserResult = {
      authenticated: true,
      user: {
        login: 'ipetrov',
        email: 'i.petrov@rpm.local',
        firstName: 'Иван',
        lastName: 'Петров',
      },
    };
    expect(result).toEqual(expected);
  });
});
