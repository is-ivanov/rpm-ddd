import type { PersonName, SortColumn, SortDirection, UserRow, UserSummaryResponse } from './users-grid.types';

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

export function filterRowsByFullName(rows: UserRow[], term: string): UserRow[] {
  const needle = term.trim().toLowerCase();
  if (needle === '') {
    return rows;
  }
  return rows.filter((row) => row.name.toLowerCase().includes(needle));
}

const STATUS_LIFECYCLE_RANK: Record<string, number> = {
  Pending: 0,
  Active: 1,
  Locked: 2,
  Inactive: 3,
};

const UNKNOWN_STATUS_RANK = Number.MAX_SAFE_INTEGER;

function statusRank(status: string): number {
  return STATUS_LIFECYCLE_RANK[status] ?? UNKNOWN_STATUS_RANK;
}

function compareByColumn(left: UserRow, right: UserRow, column: SortColumn): number {
  if (column === 'login') {
    return left.login.localeCompare(right.login);
  }
  return statusRank(left.status) - statusRank(right.status);
}

export function sortUserRows(rows: UserRow[], column: SortColumn, direction: SortDirection): UserRow[] {
  const factor = direction === 'desc' ? -1 : 1;
  return rows.toSorted((left, right) => factor * compareByColumn(left, right, column));
}
