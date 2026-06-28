import type { PersonName, UserRow, UserSummaryResponse } from './users-grid.types';

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Active',
  PENDING: 'Pending',
  LOCKED: 'Locked',
  INACTIVE: 'Inactive',
};

function toStatusLabel(status: string): string {
  return STATUS_LABELS[status] ?? status;
}

function toFullName(name: PersonName): string {
  return [name.firstName, name.middleName, name.lastName].filter(Boolean).join(' ');
}

function toActorLabel(actor: PersonName): string {
  if (actor.lastName === '') {
    return actor.firstName;
  }
  return `${actor.firstName.charAt(0)}. ${actor.lastName}`;
}

export function buildUserRows(users: UserSummaryResponse[]): UserRow[] {
  return users.map((user) => ({
    name: toFullName(user.name),
    login: user.login,
    email: user.email,
    status: toStatusLabel(user.status),
    createdBy: toActorLabel(user.audit.createdBy),
    updatedBy: toActorLabel(user.audit.updatedBy),
    createdAt: user.audit.createdAt,
    updatedAt: user.audit.updatedAt,
  }));
}
