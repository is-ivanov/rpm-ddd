import type { CreateUserRequest } from './create-user.types';

export function createUser(request: CreateUserRequest): Promise<void> {
  void request;
  return Promise.reject(new Error('createUser not implemented'));
}
