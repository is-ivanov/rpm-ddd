import type { CreateUserRequest } from './create-user.types';
import { postJsonWithCsrf } from '@/features/auth/logic/csrf';

const CREATE_USER_PATH = '/api/admin/users';

export async function createUser(request: CreateUserRequest): Promise<void> {
  await postJsonWithCsrf(CREATE_USER_PATH, request);
}
