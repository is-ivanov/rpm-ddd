import { LOGIN_PATH, shouldRedirectToLogin } from '@/app/logic/unauthorized-redirect.logic';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';
const NULL_BODY_STATUSES = new Set([101, 103, 204, 205, 304]);

export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const response = await fetch(`${BASE_URL}${path}`, init);
  const buffered = await bufferResponse(response);
  await redirectToLoginWhenUnauthorized(buffered.status);
  return buffered;
}

async function bufferResponse(response: Response): Promise<Response> {
  const buffer = await response.arrayBuffer();
  const body = NULL_BODY_STATUSES.has(response.status) ? null : buffer;
  return new Response(body, {
    status: response.status,
    statusText: response.statusText,
    headers: response.headers,
  });
}

async function redirectToLoginWhenUnauthorized(status: number): Promise<void> {
  const { router } = await import('@/router');
  if (shouldRedirectToLogin(status, router.currentRoute.value.path)) {
    await router.push(LOGIN_PATH);
  }
}
