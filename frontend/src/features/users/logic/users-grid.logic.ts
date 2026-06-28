import type { UserRow, UserSummaryResponse } from './users-grid.types';

export function buildUserRows(users: UserSummaryResponse[]): UserRow[] {
  return users.map((user) => ({
    name: user.name.firstName,
    login: user.login,
    email: user.email,
    status: user.status,
    createdBy: user.audit.createdBy.firstName,
    updatedBy: user.audit.updatedBy.firstName,
    createdAt: user.audit.createdAt,
    updatedAt: user.audit.updatedAt,
  }));
}
