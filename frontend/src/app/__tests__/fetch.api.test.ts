import { beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { server } from '@/test/msw-server';
import { apiFetch } from '@/app/logic/fetch.api';
import { useAuthStore } from '@/app/stores/auth.store';
import type { AuthenticatedUser } from '@/app/logic/current-user.types';

const BASE = import.meta.env.VITE_API_URL;

const PROTECTED_PATH = '/api/auth/me';

const JOHN_DOE: AuthenticatedUser = {
  login: 'jdoe',
  email: 'j.doe@rpm.local',
  firstName: 'John',
  lastName: 'Doe',
};

interface Problem {
  type: string;
  title: string;
  status: number;
  detail: string;
}

const UNAUTHORIZED_PROBLEM: Problem = {
  type: 'https://www.rpm-ddd.my/problem/unauthorized',
  title: 'Unauthorized',
  status: 401,
  detail: 'Full authentication is required to access this resource',
};

const FORBIDDEN_PROBLEM: Problem = {
  type: 'https://www.rpm-ddd.my/problem/forbidden',
  title: 'Forbidden',
  status: 403,
  detail: 'Access Denied',
};

function stubProblem(problem: Problem): void {
  server.use(
    http.get(`${BASE}${PROTECTED_PATH}`, () =>
      HttpResponse.json(
        { ...problem, instance: PROTECTED_PATH },
        { status: problem.status, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

describe('Shared API Fetch Layer', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('resets the auth session when an API call returns 401', async () => {
    stubProblem(UNAUTHORIZED_PROBLEM);
    const store = useAuthStore();
    store.$patch({ currentUser: JOHN_DOE });

    const response = await apiFetch(PROTECTED_PATH);

    expect(response.status).toBe(401);
    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  it('leaves the auth session intact when an API call returns 403', async () => {
    stubProblem(FORBIDDEN_PROBLEM);
    const store = useAuthStore();
    store.$patch({ currentUser: JOHN_DOE });

    const response = await apiFetch(PROTECTED_PATH);

    expect(response.status).toBe(403);
    expect(store.currentUser).toEqual(JOHN_DOE);
    expect(store.isAuthenticated).toBe(true);
  });
});
