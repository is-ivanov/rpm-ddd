import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { server } from '@/test/msw-server';
import { captureRejection } from '@/test/capture-rejection';
import { CSRF_PATH, XSRF_TOKEN, stubCsrfSetsCookie } from '@/test/csrf-stub';
import { registerUser } from '../logic/register-user.api';
import { RegisterUserError } from '../logic/register-user.types';
import type { ProblemFieldError, RegisterUserRequest } from '../logic/register-user.types';

const BASE = import.meta.env.VITE_API_URL;

const REGISTER_USER_PATH = '/api/admin/users';

const NEW_USER: RegisterUserRequest = {
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

function stubRegisterUserCapturing(captured: CapturedRequest): void {
  server.use(
    http.post(`${BASE}${REGISTER_USER_PATH}`, async ({ request }) => {
      captured.order.push(`POST ${REGISTER_USER_PATH}`);
      captured.csrfHeader = request.headers.get('X-XSRF-TOKEN');
      captured.body = await request.json();
      return new HttpResponse(null, { status: 201 });
    }),
  );
}

function stubRegisterUserValidationProblem(fieldErrors: ProblemFieldError[]): void {
  server.use(
    http.post(`${BASE}${REGISTER_USER_PATH}`, () =>
      HttpResponse.json(
        {
          type: 'https://www.rpm-ddd.my/problem/validation-failed',
          title: 'Unprocessable Content',
          status: 422,
          detail: `Validation failed for object='registerUserRequest'. Error count: ${fieldErrors.length}.`,
          instance: REGISTER_USER_PATH,
          fieldErrors: fieldErrors.map((fieldError) => ({
            code: 'DuplicateKey',
            property: fieldError.property,
            message: fieldError.message,
            rejectedValue: '',
            path: fieldError.property,
          })),
        },
        { status: 422, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

describe('Register User API Client', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  // RED (Scn 4.2): register-user.api.ts::registerUser was an unimplemented stub that threw
  // ('registerUser not implemented'), so the POST never went in flight and the captured body/header
  // stayed undefined. GREEN wires registerUser through postJsonWithCsrf(REGISTER_USER_PATH, request)
  // so the CSRF handshake runs and the request resolves on 201. The resolve + strict body/header
  // equality below is the pinned RED reason so an incidental failure isn't absorbed by it.fails().
  it('performs the CSRF handshake and POSTs the register-user body with the X-XSRF-TOKEN header', async () => {
    const captured: CapturedRequest = { order: [] };
    stubCsrfSetsCookie(captured);
    stubRegisterUserCapturing(captured);

    await registerUser(NEW_USER);

    expect(captured.order).toEqual([`GET ${CSRF_PATH}`, `POST ${REGISTER_USER_PATH}`]);
    expect(captured.csrfHeader).toBe(XSRF_TOKEN);
    expect(captured.body).toEqual(NEW_USER);
  });

  it('rejects with a RegisterUserError carrying parsed fieldErrors on 422 duplicate login', async () => {
    stubCsrfSetsCookie({ order: [] });
    stubRegisterUserValidationProblem([{ property: 'login', message: 'Login already exists' }]);

    const error = await captureRejection(registerUser(NEW_USER));

    expect(error).toBeInstanceOf(RegisterUserError);
    expect((error as RegisterUserError).fieldErrors).toEqual([{ property: 'login', message: 'Login already exists' }]);
  });
});
