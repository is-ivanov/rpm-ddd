import { apiFetch } from '@/app/logic/fetch.api';

function readCookie(name: string): string {
  const match = new RegExp(`(?:^|; )${name}=([^;]*)`).exec(document.cookie);
  return match ? decodeURIComponent(match[1]) : '';
}

async function primeCsrfToken(): Promise<string> {
  await apiFetch('/api/auth/csrf', {
    method: 'GET',
    credentials: 'include',
  });

  return readCookie('XSRF-TOKEN');
}

export async function postJsonWithCsrf(path: string, body: unknown): Promise<Response> {
  const xsrfToken = await primeCsrfToken();

  return apiFetch(path, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': xsrfToken,
    },
    body: JSON.stringify(body),
  });
}
