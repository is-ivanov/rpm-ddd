import { LOGIN_PATH, shouldRedirectToLogin } from '@/app/logic/unauthorized-redirect.logic';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const response = await fetch(`${BASE_URL}${path}`, init);
  await redirectToLoginWhenUnauthorized(response.status);
  return response;
}

async function redirectToLoginWhenUnauthorized(status: number): Promise<void> {
  const { router } = await import('@/router');
  if (shouldRedirectToLogin(status, router.currentRoute.value.path)) {
    await router.push(LOGIN_PATH);
  }
}
