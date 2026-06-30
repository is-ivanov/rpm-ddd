import { beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { issue } from 'allure-js-commons';
import { server } from '@/test/msw-server';
import { useAuthStore } from '@/app/stores/auth.store';
import { fetchAdminUsers } from '../logic/admin-users.api';
import type { UserSummaryResponse } from '../logic/users-grid.types';
import type { AuthenticatedUser } from '@/app/logic/current-user.types';

const BASE = import.meta.env.VITE_API_URL;

const ADMIN_USERS_PATH = '/api/admin/users';

const SEEDED_VIEWER: AuthenticatedUser = {
  login: 'jdoe',
  email: 'j.doe@rpm.local',
  firstName: 'John',
  lastName: 'Doe',
  timeZone: 'Europe/Berlin',
};

const UNAUTHORIZED_PROBLEM = {
  type: 'https://www.rpm-ddd.my/problem/unauthorized',
  title: 'Unauthorized',
  status: 401,
  detail: 'Full authentication is required to access this resource',
  instance: ADMIN_USERS_PATH,
};

const ADMIN_USERS: UserSummaryResponse[] = [
  {
    userId: '00000000-0000-0000-0000-000000000001',
    name: { firstName: 'Sarah', middleName: 'Jane', lastName: 'Connor' },
    login: 's.connor',
    email: 's.connor@rpm.local',
    status: 'ACTIVE',
    audit: {
      createdAt: '2026-06-22T14:30:51.217Z',
      createdBy: { firstName: 'John', middleName: 'Robert', lastName: 'Doe' },
      updatedAt: '2026-06-24T08:11:42.905Z',
      updatedBy: { firstName: 'Sarah', middleName: 'Jane', lastName: 'Connor' },
    },
  },
  {
    userId: '00000000-0000-0000-0000-000000000002',
    name: { firstName: 'Michael', middleName: null, lastName: 'Scott' },
    login: 'm.scott',
    email: 'm.scott@rpm.local',
    status: 'PENDING',
    audit: {
      createdAt: '2026-06-20T11:02:09.310Z',
      createdBy: { firstName: 'System', middleName: null, lastName: '' },
      updatedAt: '2026-06-21T13:33:27.064Z',
      updatedBy: { firstName: 'John', middleName: 'Robert', lastName: 'Doe' },
    },
  },
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

  // RED (#250): fetchAdminUsers uses raw fetch() instead of apiFetch(), so a 401 never runs
  // resetSessionWhenUnauthorized() and the auth store keeps its stale authenticated state. GREEN
  // routes the client through apiFetch(), which resets the store on 401. The store assertion below
  // is the pinned RED reason (the ZodError thrown while parsing the problem body is swallowed).
  it.fails('resets the auth session when GET /api/admin/users returns 401', async () => {
    await issue('250');
    stubAdminUsers(UNAUTHORIZED_PROBLEM, {
      status: 401,
      headers: { 'Content-Type': 'application/problem+json' },
    });
    const store = useAuthStore();
    store.$patch({ currentUser: SEEDED_VIEWER });

    await fetchAdminUsers().catch(() => {});

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });
});
