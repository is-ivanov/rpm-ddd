import { describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { server } from '@/test/msw-server';
import { fetchAdminUsers } from '../logic/admin-users.api';
import type { UserSummaryResponse } from '../logic/users-grid.types';

const BASE = import.meta.env.VITE_API_URL;

const ADMIN_USERS_PATH = '/api/admin/users';

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
  it('returns the parsed user summaries when GET /api/admin/users returns 200', async () => {
    stubAdminUsers(ADMIN_USERS, { status: 200 });

    const result = await fetchAdminUsers().catch((error: unknown) => error);

    expect(result).toEqual(ADMIN_USERS);
  });
});
