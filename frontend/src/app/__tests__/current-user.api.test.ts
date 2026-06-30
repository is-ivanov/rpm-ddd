import { describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { server } from '@/test/msw-server';
import { fetchCurrentUser } from '../logic/current-user.api';
import type { CurrentUserResult } from '../logic/current-user.types';

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
      login: 'jdoe',
      email: 'j.doe@rpm.local',
      firstName: 'John',
      lastName: 'Doe',
      status: 'ACTIVE',
      roles: [],
      timeZone: 'Europe/Berlin',
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

  // RED (Story 4 Scn 3.3): the Users-grid timestamp tooltip renders createdAt/updatedAt in the
  // viewer's profile timezone, which flows from GET /api/auth/me. The current-user contract must
  // therefore surface `timeZone` (an IANA zone id). Today currentUserResponseSchema has no
  // `timeZone` key and z.object STRIPS unknown keys, so the parsed user drops it -- the toHaveProperty
  // below fails. GREEN adds `timeZone` to the schema (+ AuthenticatedUser type / auth store). The
  // value 'Europe/Berlin' is lock-step with VIEWER_TIME_ZONE_ID in the E2E
  // users-grid-time.fixture, keeping the unit contract and the browser contract identical.
  it('maps an authenticated result carrying the viewer timeZone when GET /api/auth/me returns 200', async () => {
    stubMeAuthenticated();

    const result = await fetchCurrentUser();

    const expected: CurrentUserResult = {
      authenticated: true,
      user: {
        login: 'jdoe',
        email: 'j.doe@rpm.local',
        firstName: 'John',
        lastName: 'Doe',
        timeZone: 'Europe/Berlin',
      },
    };
    expect(result).toEqual(expected);
  });
});
