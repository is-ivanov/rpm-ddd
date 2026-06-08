const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export function readCookie(name: string): string {
  const match = new RegExp(`(?:^|; )${name}=([^;]*)`).exec(document.cookie);
  return match ? decodeURIComponent(match[1]) : '';
}

export async function primeCsrfToken(): Promise<string> {
  await fetch(`${BASE_URL}/api/auth/csrf`, {
    method: 'GET',
    credentials: 'include',
  });

  return readCookie('XSRF-TOKEN');
}
