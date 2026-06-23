import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { server } from '@/test/msw-server';
import { useAuthStore } from '../auth.store';
import type { AuthenticatedUser } from '@/features/home/logic/types';

const BASE = import.meta.env.VITE_API_URL;

const ME_PATH = '/api/auth/me';

const IVAN_PETROV: AuthenticatedUser = {
  login: 'ipetrov',
  email: 'i.petrov@rpm.local',
  firstName: 'Иван',
  lastName: 'Петров',
};

function stubMe(body: JsonBodyType, init: ResponseInit): void {
  server.use(http.get(`${BASE}${ME_PATH}`, () => HttpResponse.json(body, init)));
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

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('starts unauthenticated with no current user', () => {
    const store = useAuthStore();

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
    expect(store.dashboardUser).toBeNull();
  });

  // RED — loadMe not implemented (throws 'Not implemented')
  it.fails('loads the current user and exposes the dashboard view model on a 200 response', async () => {
    stubMeAuthenticated();
    const store = useAuthStore();

    await store.loadMe();

    expect(store.currentUser).toEqual(IVAN_PETROV);
    expect(store.isAuthenticated).toBe(true);
    expect(store.dashboardUser).toEqual({
      displayName: 'Иван Петров',
      initials: 'ИП',
      email: 'i.petrov@rpm.local',
    });
  });

  // RED — loadMe not implemented (throws 'Not implemented')
  it.fails('stays unauthenticated when /me returns 401', async () => {
    stubMeUnauthenticated();
    const store = useAuthStore();

    await store.loadMe();

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  // RED — reset not implemented (throws 'Not implemented')
  it.fails('clears the current user on reset', () => {
    const store = useAuthStore();
    store.$patch({ currentUser: IVAN_PETROV });

    store.reset();

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });
});
