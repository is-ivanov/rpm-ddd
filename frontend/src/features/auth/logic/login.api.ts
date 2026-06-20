import type { LoginRequest } from './types';
import { LoginError } from './types';
import { postJsonWithCsrf } from './csrf';
import { problemDetailSchema } from '@/app/schemas/problem-detail.schema';

const AUTHENTICATION_FAILED_TYPE = 'https://www.rpm-ddd.my/problem/authentication-failed';

async function throwLoginError(response: Response): Promise<never> {
  const problem = problemDetailSchema.parse(await response.json());
  throw new LoginError(problem.detail, problem.type === AUTHENTICATION_FAILED_TYPE, problem.fieldErrors ?? []);
}

export async function login(request: LoginRequest): Promise<void> {
  const response = await postJsonWithCsrf('/api/auth/login', request);

  if (!response.ok) {
    await throwLoginError(response);
  }
}
