import { http, HttpResponse } from 'msw';
import { server } from './msw-server';

const BASE = import.meta.env.VITE_API_URL;

export const XSRF_TOKEN = 'test-xsrf-token';
export const CSRF_PATH = '/api/auth/csrf';

export interface CsrfCapture {
  order: string[];
}

/** Stubs GET /api/auth/csrf to set the XSRF-TOKEN cookie and records the call order. */
export function stubCsrfSetsCookie(captured: CsrfCapture): void {
  server.use(
    http.get(`${BASE}${CSRF_PATH}`, () => {
      captured.order.push(`GET ${CSRF_PATH}`);
      document.cookie = `XSRF-TOKEN=${XSRF_TOKEN}; Path=/`;
      return HttpResponse.json({}, { status: 200 });
    }),
  );
}
