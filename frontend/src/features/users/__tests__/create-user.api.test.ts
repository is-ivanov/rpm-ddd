import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { server } from '@/test/msw-server';
import { CSRF_PATH, XSRF_TOKEN, stubCsrfSetsCookie } from '@/test/csrf-stub';
import { createUser } from '../logic/create-user.api';
import type { CreateUserRequest } from '../logic/create-user.types';

const BASE = import.meta.env.VITE_API_URL;

const CREATE_USER_PATH = '/api/admin/users';

const NEW_USER: CreateUserRequest = {
  firstName: 'Sarah',
  middleName: 'Jane',
  lastName: 'Connor',
  login: 's.connor',
  email: 's.connor@rpm.local',
  timeZone: 'Europe/Berlin',
};

interface CapturedRequest {
  order: string[];
  csrfHeader?: string | null;
  body?: unknown;
}

function stubCreateUserCapturing(captured: CapturedRequest): void {
  server.use(
    http.post(`${BASE}${CREATE_USER_PATH}`, async ({ request }) => {
      captured.order.push(`POST ${CREATE_USER_PATH}`);
      captured.csrfHeader = request.headers.get('X-XSRF-TOKEN');
      captured.body = await request.json();
      return new HttpResponse(null, { status: 201 });
    }),
  );
}

describe('Create User API Client', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  // RED (Scn 4.2): create-user.api.ts::createUser is an unimplemented stub that throws
  // ('createUser not implemented'), so the POST never goes in flight and the captured body/header
  // stay undefined. GREEN wires createUser through postJsonWithCsrf(CREATE_USER_PATH, request) so
  // the CSRF handshake runs and the request resolves on 201. The resolve + strict body/header
  // equality below is the pinned RED reason so an incidental failure isn't absorbed by it.fails().
  it('performs the CSRF handshake and POSTs the create-user body with the X-XSRF-TOKEN header', async () => {
    const captured: CapturedRequest = { order: [] };
    stubCsrfSetsCookie(captured);
    stubCreateUserCapturing(captured);

    await createUser(NEW_USER);

    expect(captured.order).toEqual([`GET ${CSRF_PATH}`, `POST ${CREATE_USER_PATH}`]);
    expect(captured.csrfHeader).toBe(XSRF_TOKEN);
    expect(captured.body).toEqual(NEW_USER);
  });
});
