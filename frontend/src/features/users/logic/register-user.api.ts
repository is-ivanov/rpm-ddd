import type { RegisterUserRequest } from './register-user.types';
import { RegisterUserError } from './register-user.types';
import { postJsonWithCsrf } from '@/features/auth/logic/csrf';
import { problemDetailSchema } from '@/app/schemas/problem-detail.schema';

const REGISTER_USER_PATH = '/api/admin/users';

async function throwRegisterUserError(response: Response): Promise<never> {
  const problem = problemDetailSchema.parse(await response.json());
  throw new RegisterUserError(problem.detail, problem.fieldErrors ?? []);
}

export async function registerUser(request: RegisterUserRequest): Promise<void> {
  const response = await postJsonWithCsrf(REGISTER_USER_PATH, request);

  if (!response.ok) {
    await throwRegisterUserError(response);
  }
}
