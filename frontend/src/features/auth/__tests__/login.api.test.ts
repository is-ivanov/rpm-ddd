import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { createPinia, setActivePinia } from 'pinia';
import { issue } from 'allure-js-commons';
import { server } from '@/test/msw-server';
import { captureRejection } from '@/test/capture-rejection';
import { CSRF_PATH, XSRF_TOKEN, stubCsrfSetsCookie } from '@/test/csrf-stub';
import { login } from '../logic/login.api';
import { LoginError } from '../logic/types';
import type { LoginRequest, ProblemFieldError } from '../logic/types';

const BASE = import.meta.env.VITE_API_URL;

const LOGIN_PATH = '/api/auth/login';

const VALID_CREDENTIALS: LoginRequest = { login: 'ivan', password: 'right-pass' };

interface CapturedRequest {
  order: string[];
  csrfHeader?: string | null;
  body?: unknown;
}

function stubLoginCapturing(captured: CapturedRequest): void {
  server.use(
    http.post(`${BASE}${LOGIN_PATH}`, async ({ request }) => {
      captured.order.push(`POST ${LOGIN_PATH}`);
      captured.csrfHeader = request.headers.get('X-XSRF-TOKEN');
      captured.body = await request.json();
      return HttpResponse.json({}, { status: 200 });
    }),
  );
}

function stubLoginProblem(problem: { type: string; detail: string }): void {
  server.use(
    http.post(`${BASE}${LOGIN_PATH}`, () =>
      HttpResponse.json(
        {
          type: problem.type,
          title: 'Unauthorized',
          status: 401,
          detail: problem.detail,
          instance: LOGIN_PATH,
        },
        { status: 401, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

function stubLoginValidationProblem(fieldErrors: ProblemFieldError[]): void {
  server.use(
    http.post(`${BASE}${LOGIN_PATH}`, () =>
      HttpResponse.json(
        {
          type: 'https://www.rpm-ddd.my/problem/validation-failed',
          title: 'Unprocessable Content',
          status: 422,
          detail: `Validation failed for object='loginRequest'. Error count: ${fieldErrors.length}.`,
          instance: LOGIN_PATH,
          fieldErrors: fieldErrors.map((fieldError) => ({
            code: 'NotBlank',
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

describe('Login API Client', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  it('performs the CSRF handshake before posting login with the X-XSRF-TOKEN header', async () => {
    await issue('129');
    const captured: CapturedRequest = { order: [] };
    stubCsrfSetsCookie(captured);
    stubLoginCapturing(captured);

    await login(VALID_CREDENTIALS);

    expect(captured.order).toEqual([`GET ${CSRF_PATH}`, `POST ${LOGIN_PATH}`]);
    expect(captured.csrfHeader).toBe(XSRF_TOKEN);
    expect(captured.body).toEqual(VALID_CREDENTIALS);
  });

  it('surfaces the problem+json detail as a LoginError on 401 invalid credentials', async () => {
    stubCsrfSetsCookie({ order: [] });
    stubLoginProblem({
      type: 'https://www.rpm-ddd.my/problem/bad-credentials',
      detail: 'Bad credentials',
    });

    const error = await captureRejection(login({ login: 'ivan', password: 'wrong-pass' }));

    expect(error).toBeInstanceOf(LoginError);
    expect((error as LoginError).message).toBe('Bad credentials');
    expect((error as LoginError).requiresActivation).toBe(false);
  });

  it('flags requiresActivation on 401 account not activated', async () => {
    stubCsrfSetsCookie({ order: [] });
    stubLoginProblem({
      type: 'https://www.rpm-ddd.my/problem/authentication-failed',
      detail: 'Account not activated',
    });

    const error = await captureRejection(login(VALID_CREDENTIALS));

    expect(error).toBeInstanceOf(LoginError);
    expect((error as LoginError).message).toBe('Account not activated');
    expect((error as LoginError).requiresActivation).toBe(true);
  });

  it('parses 422 ProblemDetail fieldErrors into a LoginError carrying structured field errors', async () => {
    await issue('131');
    stubCsrfSetsCookie({ order: [] });
    stubLoginValidationProblem([
      { property: 'login', message: 'must not be blank' },
      { property: 'password', message: 'must not be blank' },
    ]);

    const error = await captureRejection(login({ login: '', password: '' }));

    expect(error).toBeInstanceOf(LoginError);
    expect((error as LoginError).fieldErrors).toEqual([
      { property: 'login', message: 'must not be blank' },
      { property: 'password', message: 'must not be blank' },
    ]);
  });
});
