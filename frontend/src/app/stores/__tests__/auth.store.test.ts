import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { server } from '@/test/msw-server';
import { CSRF_PATH, stubCsrfSetsCookie, type CsrfCapture } from '@/test/csrf-stub';
import { useAuthStore } from '../auth.store';
import { anAuthenticatedUser } from '@/test/builders/authenticated-user';
import { aCurrentUserResponse, anUnauthenticatedProblem } from '@/test/builders/current-user-response';

const BASE = import.meta.env.VITE_API_URL;

const ME_PATH = '/api/auth/me';
const LOGOUT_PATH = '/api/auth/logout';

const JOHN_DOE = anAuthenticatedUser();

function stubMe(body: JsonBodyType, init: ResponseInit): void {
  server.use(http.get(`${BASE}${ME_PATH}`, () => HttpResponse.json(body, init)));
}

function stubMeAuthenticated(): void {
  stubMe(aCurrentUserResponse(), { status: 200 });
}

function stubLogoutCapturing(captured: CsrfCapture): void {
  server.use(
    http.post(`${BASE}${LOGOUT_PATH}`, () => {
      captured.order.push(`POST ${LOGOUT_PATH}`);
      return new HttpResponse(null, { status: 200 });
    }),
  );
}

function stubMeUnauthenticated(): void {
  stubMe(anUnauthenticatedProblem(), { status: 401, headers: { 'Content-Type': 'application/problem+json' } });
}

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('starts unauthenticated with no current user', () => {
    const store = useAuthStore();

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
    expect(store.dashboardUser).toBeNull();
  });

  it('loads the current user and exposes the dashboard view model on a 200 response', async () => {
    stubMeAuthenticated();
    const store = useAuthStore();

    await store.loadMe();

    expect(store.currentUser).toEqual(JOHN_DOE);
    expect(store.isAuthenticated).toBe(true);
    expect(store.dashboardUser).toEqual({
      displayName: 'John Doe',
      initials: 'JD',
      email: 'j.doe@rpm.local',
    });
  });

  it('stays unauthenticated when /me returns 401', async () => {
    stubMeUnauthenticated();
    const store = useAuthStore();

    await store.loadMe();

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  it('logs out via the API then clears the current user', async () => {
    const captured: CsrfCapture = { order: [] };
    stubCsrfSetsCookie(captured);
    stubLogoutCapturing(captured);
    const store = useAuthStore();
    store.$patch({ currentUser: JOHN_DOE });

    await store.logout();

    expect(captured.order).toEqual([`GET ${CSRF_PATH}`, `POST ${LOGOUT_PATH}`]);
    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });

  it('clears the current user on reset', () => {
    const store = useAuthStore();
    store.$patch({ currentUser: JOHN_DOE });

    store.reset();

    expect(store.currentUser).toBeNull();
    expect(store.isAuthenticated).toBe(false);
  });
});
