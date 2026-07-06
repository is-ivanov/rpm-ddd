import { beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { issue } from 'allure-js-commons';
import { server } from '@/test/msw-server';
import { useAuthStore } from '../stores/auth.store';
import { fetchCurrentUser } from '../logic/current-user.api';
import type { CurrentUserResult } from '../logic/current-user.types';
import { anAuthenticatedUser } from '@/test/builders/authenticated-user';
import { aCurrentUserResponse, anUnauthenticatedProblem } from '@/test/builders/current-user-response';

const BASE = import.meta.env.VITE_API_URL;

const ME_PATH = '/api/auth/me';

const SEEDED_VIEWER = anAuthenticatedUser();

function stubMe(body: JsonBodyType, init: ResponseInit): void {
  server.use(http.get(`${BASE}${ME_PATH}`, () => HttpResponse.json(body, init)));
}

function stubMeUnauthenticated(): void {
  stubMe(anUnauthenticatedProblem(), { status: 401, headers: { 'Content-Type': 'application/problem+json' } });
}

function stubMeAuthenticated(): void {
  stubMe(aCurrentUserResponse(), { status: 200 });
}

describe('Current User API Client', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  // GET /api/auth/me -> 401 routes through apiFetch(), so resetSessionWhenUnauthorized()
  // clears a previously-authenticated store while the client maps 401 to { authenticated: false }.
  it('resets the auth session when GET /api/auth/me returns 401', async () => {
    await issue('250');
    stubMeUnauthenticated();
    const store = useAuthStore();
    store.$patch({ currentUser: SEEDED_VIEWER });

    const result = await fetchCurrentUser();

    const expected: CurrentUserResult = { authenticated: false };
    expect(result).toEqual(expected);
    expect(
      store.currentUser,
      'regression #250: a raw fetch() bypassed the reset, leaving stale authenticated state',
    ).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  it('surfaces an unauthenticated result when GET /api/auth/me returns 401', async () => {
    stubMeUnauthenticated();

    const result = await fetchCurrentUser().catch((error: unknown) => error);

    const expected: CurrentUserResult = { authenticated: false };
    expect(result).toEqual(expected);
  });

  // The Users-grid timestamp tooltip renders createdAt/updatedAt in the viewer's profile
  // timezone, which flows from GET /api/auth/me, so the current-user contract surfaces
  // `timeZone` (an IANA zone id) on the schema + AuthenticatedUser type / auth store. The value
  // 'Europe/Berlin' is lock-step with VIEWER_TIME_ZONE_ID in the E2E users-grid-time.fixture,
  // keeping the unit contract and the browser contract identical (Story 4 Scn 3.3).
  it('maps an authenticated result carrying the viewer timeZone when GET /api/auth/me returns 200', async () => {
    stubMeAuthenticated();

    const result = await fetchCurrentUser();

    const expected: CurrentUserResult = { authenticated: true, user: anAuthenticatedUser() };
    expect(result).toEqual(expected);
  });
});
