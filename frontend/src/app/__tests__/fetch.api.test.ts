import { beforeEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { issue } from 'allure-js-commons';
import { server } from '@/test/msw-server';
import { captureRejection } from '@/test/capture-rejection';
import { router } from '@/router';
import { apiFetch } from '@/app/logic/fetch.api';
import { validateActivationToken } from '@/features/auth/logic/activation.api';
import { ActivationError } from '@/features/auth/logic/types';

const BASE = import.meta.env.VITE_API_URL;

const ACTIVATE_PATH = '/api/auth/activate';

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

function stubActivateProblem(problem: Problem): void {
  server.use(
    http.get(`${BASE}${ACTIVATE_PATH}`, () =>
      HttpResponse.json(
        { ...problem, instance: ACTIVATE_PATH },
        { status: problem.status, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

describe('Shared API Fetch Layer', () => {
  beforeEach(async () => {
    await router.push('/activate');
  });

  it('navigates to the login route when a protected API call returns 401', async () => {
    await issue('162');
    stubActivateProblem(UNAUTHORIZED_PROBLEM);

    const response = await apiFetch(`${ACTIVATE_PATH}?token=expired-session`);

    expect(response.status).toBe(401);
    expect(router.currentRoute.value.path).toBe('/login');
  });

  it('stays on the current route when an API call returns 403', async () => {
    await issue('162');
    stubActivateProblem(FORBIDDEN_PROBLEM);

    const response = await apiFetch(ACTIVATE_PATH);

    expect(response.status).toBe(403);
    expect(router.currentRoute.value.path).toBe('/activate');
  });

  it('routes activation token validation through the shared layer so a 401 lands on the login route', async () => {
    await issue('162');
    stubActivateProblem(UNAUTHORIZED_PROBLEM);

    const error = await captureRejection(validateActivationToken('expired-session-token'));

    expect(error).toBeInstanceOf(ActivationError);
    expect((error as ActivationError).message).toBe('Full authentication is required to access this resource');
    expect(router.currentRoute.value.path).toBe('/login');
  });
});
