import type { RegisterUserRequest } from './register-user.types';
import { postJsonWithCsrf } from '@/features/auth/logic/csrf';

const REGISTER_USER_PATH = '/api/admin/users';

export async function registerUser(request: RegisterUserRequest): Promise<void> {
  await postJsonWithCsrf(REGISTER_USER_PATH, request);
}
