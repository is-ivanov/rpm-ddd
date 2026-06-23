import { isUnauthorized } from '@/app/logic/unauthorized-redirect.logic';

export const API_BASE_URL = import.meta.env.VITE_API_URL ?? '';
const NULL_BODY_STATUSES = new Set([101, 103, 204, 205, 304]);

export function apiUrl(path: string): string {
  return `${API_BASE_URL}${path}`;
}

export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const response = await fetch(apiUrl(path), init);
  const buffered = await bufferResponse(response);
  await resetSessionWhenUnauthorized(buffered.status);
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

async function resetSessionWhenUnauthorized(status: number): Promise<void> {
  if (isUnauthorized(status)) {
    const { useAuthStore } = await import('@/app/stores/auth.store');
    useAuthStore().reset();
  }
}
