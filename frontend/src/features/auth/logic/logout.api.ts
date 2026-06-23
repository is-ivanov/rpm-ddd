import { postJsonWithCsrf } from './csrf';

export async function logout(): Promise<void> {
  await postJsonWithCsrf('/api/auth/logout', {});
}
