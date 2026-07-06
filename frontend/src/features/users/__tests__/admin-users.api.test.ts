import { beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { issue } from 'allure-js-commons';
import { server } from '@/test/msw-server';
import { useAuthStore } from '@/app/stores/auth.store';
import { fetchAdminUsers } from '../logic/admin-users.api';
import type { UserSummaryResponse } from '../logic/users-grid.types';
import { anAuthenticatedUser } from '@/test/builders/authenticated-user';
import { JOHN_DOE, MICHAEL_SCOTT, SYSTEM_ACTOR, aUserSummary } from '@/test/builders/user-summary';

const BASE = import.meta.env.VITE_API_URL;

const ADMIN_USERS_PATH = '/api/admin/users';

const SEEDED_VIEWER = anAuthenticatedUser();

const UNAUTHORIZED_PROBLEM = {
  type: 'https://www.rpm-ddd.my/problem/unauthorized',
  title: 'Unauthorized',
  status: 401,
  detail: 'Full authentication is required to access this resource',
  instance: ADMIN_USERS_PATH,
};

const ADMIN_USERS: UserSummaryResponse[] = [
  aUserSummary(),
  aUserSummary({
    userId: '00000000-0000-0000-0000-000000000002',
    name: MICHAEL_SCOTT,
    login: 'm.scott',
    email: 'm.scott@rpm.local',
    status: 'PENDING',
    audit: {
      createdAt: '2026-06-20T11:02:09.310Z',
      createdBy: SYSTEM_ACTOR,
      updatedAt: '2026-06-21T13:33:27.064Z',
      updatedBy: JOHN_DOE,
    },
  }),
];

function stubAdminUsers(body: JsonBodyType, init: ResponseInit): void {
  server.use(http.get(`${BASE}${ADMIN_USERS_PATH}`, () => HttpResponse.json(body, init)));
}

describe('Admin Users API Client', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('returns the parsed user summaries when GET /api/admin/users returns 200', async () => {
    stubAdminUsers(ADMIN_USERS, { status: 200 });

    const result = await fetchAdminUsers().catch((error: unknown) => error);

    expect(result).toEqual(ADMIN_USERS);
  });

  // GET /api/admin/users -> 401 routes through apiFetch(), so resetSessionWhenUnauthorized()
  // clears a previously-authenticated store (the ZodError from parsing the problem body is swallowed).
  it('resets the auth session when GET /api/admin/users returns 401', async () => {
    await issue('250');
    stubAdminUsers(UNAUTHORIZED_PROBLEM, {
      status: 401,
      headers: { 'Content-Type': 'application/problem+json' },
    });
    const store = useAuthStore();
    store.$patch({ currentUser: SEEDED_VIEWER });

    await fetchAdminUsers().catch(() => {});

    expect(
      store.currentUser,
      'regression #250: a raw fetch() bypassed the reset, leaving stale authenticated state',
    ).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });
});
