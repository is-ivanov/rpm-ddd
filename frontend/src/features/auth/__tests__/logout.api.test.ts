import { afterEach, describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import { CSRF_PATH, XSRF_TOKEN, stubCsrfSetsCookie } from '@/test/csrf-stub';
import { logout } from '../logic/logout.api';

const BASE = import.meta.env.VITE_API_URL;

const LOGOUT_PATH = '/api/auth/logout';

interface CapturedRequest {
  order: string[];
  csrfHeader?: string | null;
}

function stubLogoutCapturing(captured: CapturedRequest): void {
  server.use(
    http.post(`${BASE}${LOGOUT_PATH}`, ({ request }) => {
      captured.order.push(`POST ${LOGOUT_PATH}`);
      captured.csrfHeader = request.headers.get('X-XSRF-TOKEN');
      return new HttpResponse(null, { status: 200 });
    }),
  );
}

describe('Logout API Client', () => {
  afterEach(() => {
    document.cookie = 'XSRF-TOKEN=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT';
  });

  // RED — logout not implemented yet; must perform the CSRF handshake then POST
  // /api/auth/logout carrying the X-XSRF-TOKEN header. Pinned via the toEqual below:
  // capture resolve-or-reject so the assertion always runs and fails for the
  // predicted reason (no requests recorded), not because the stub throws first.
  it.fails('performs the CSRF handshake before posting logout with the X-XSRF-TOKEN header', async () => {
    const captured: CapturedRequest = { order: [] };
    stubCsrfSetsCookie(captured);
    stubLogoutCapturing(captured);

    await logout().catch((error: unknown) => error);

    expect(captured.order).toEqual([`GET ${CSRF_PATH}`, `POST ${LOGOUT_PATH}`]);
    expect(captured.csrfHeader).toBe(XSRF_TOKEN);
  });
});
