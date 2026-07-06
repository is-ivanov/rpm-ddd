import type {
  AbsoluteTimeParts,
  PersonName,
  SortColumn,
  SortDirection,
  TextFilterColumn,
  UserRow,
  UserSummaryResponse,
} from './users-grid.types';

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

function toUserRow(user: UserSummaryResponse): UserRow {
  const { audit } = user;
  return {
    name: toFullName(user.name),
    login: user.login,
    email: user.email,
    status: toStatusLabel(user.status),
    createdBy: toActorLabel(audit.createdBy),
    updatedBy: toActorLabel(audit.updatedBy),
    createdAt: audit.createdAt,
    updatedAt: audit.updatedAt,
  };
}

export function buildUserRows(users: UserSummaryResponse[]): UserRow[] {
  return users.map(toUserRow);
}

export function filterRowsByFullName(rows: UserRow[], term: string): UserRow[] {
  return filterRowsByColumns(rows, { name: term });
}

export function filterRowsByColumns(rows: UserRow[], filters: Partial<Record<TextFilterColumn, string>>): UserRow[] {
  const activeTerms = toActiveTerms(filters);
  return rows.filter((row) => activeTerms.every(([column, needle]) => row[column].toLowerCase().includes(needle)));
}

export function filterRowsByStatuses(rows: UserRow[], selected: readonly string[]): UserRow[] {
  if (selected.length === 0) {
    return rows;
  }
  return rows.filter((row) => selected.includes(row.status));
}

function toActiveTerms(filters: Partial<Record<TextFilterColumn, string>>): [TextFilterColumn, string][] {
  return (Object.entries(filters) as [TextFilterColumn, string][])
    .map(([column, term]): [TextFilterColumn, string] => [column, term.trim().toLowerCase()])
    .filter(([, needle]) => needle !== '');
}

const STATUS_LIFECYCLE_RANK: Record<string, number> = {
  Pending: 0,
  Active: 1,
  Locked: 2,
  Inactive: 3,
};

export const STATUS_FILTER_OPTIONS: readonly string[] = Object.keys(STATUS_LIFECYCLE_RANK);

const UNKNOWN_STATUS_RANK = Number.MAX_SAFE_INTEGER;

function statusRank(status: string): number {
  return STATUS_LIFECYCLE_RANK[status] ?? UNKNOWN_STATUS_RANK;
}

const TIMESTAMP_FIELD = { created: 'createdAt', updated: 'updatedAt' } as const;

function compareByColumn(left: UserRow, right: UserRow, column: SortColumn): number {
  if (column === 'status') {
    return statusRank(left.status) - statusRank(right.status);
  }
  if (column === 'created' || column === 'updated') {
    const field = TIMESTAMP_FIELD[column];
    return instant(left[field]) - instant(right[field]);
  }
  return left[column].localeCompare(right[column]);
}

function instant(isoTimestamp: string): number {
  return new Date(isoTimestamp).getTime();
}

export function sortUserRows(rows: UserRow[], column: SortColumn, direction: SortDirection): UserRow[] {
  const factor = direction === 'desc' ? -1 : 1;
  return rows.toSorted((left, right) => factor * compareByColumn(left, right, column));
}

function timeAgo(count: number, unit: string): string {
  return `${count} ${unit}${count === 1 ? '' : 's'} ago`;
}

export function toRelativeTimeLabel(isoTimestamp: string, now: Date): string {
  const elapsedSeconds = Math.floor((now.getTime() - instant(isoTimestamp)) / 1000);
  if (elapsedSeconds < 60) {
    return 'just now';
  }
  const minutes = Math.floor(elapsedSeconds / 60);
  if (minutes < 60) {
    return timeAgo(minutes, 'minute');
  }
  const hours = Math.floor(elapsedSeconds / 3600);
  if (hours < 24) {
    return timeAgo(hours, 'hour');
  }
  const days = Math.floor(elapsedSeconds / 86400);
  if (days < 7) {
    return timeAgo(days, 'day');
  }
  if (days < 30) {
    return timeAgo(Math.floor(days / 7), 'week');
  }
  if (days < 365) {
    return timeAgo(Math.floor(days / 30), 'month');
  }
  return timeAgo(Math.floor(days / 365), 'year');
}

function partValue(parts: Intl.DateTimeFormatPart[], type: Intl.DateTimeFormatPartTypes): string {
  return parts.find((part) => part.type === type)?.value ?? '';
}

export function toAbsoluteTooltipParts(isoTimestamp: string, timeZone: string): AbsoluteTimeParts {
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
    timeZoneName: 'short',
  }).formatToParts(new Date(isoTimestamp));
  return {
    date: `${partValue(parts, 'year')}-${partValue(parts, 'month')}-${partValue(parts, 'day')}`,
    time: `${partValue(parts, 'hour')}:${partValue(parts, 'minute')}`,
    tzLabel: partValue(parts, 'timeZoneName'),
    ianaZone: timeZone,
  };
}
