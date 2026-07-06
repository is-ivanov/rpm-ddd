export interface PersonName {
  readonly firstName: string;
  readonly middleName: string | null;
  readonly lastName: string;
}

export interface UserAudit {
  readonly createdAt: string;
  readonly createdBy: PersonName;
  readonly updatedAt: string;
  readonly updatedBy: PersonName;
}

export interface UserSummaryResponse {
  readonly userId: string;
  readonly name: PersonName;
  readonly login: string;
  readonly email: string;
  readonly status: string;
  readonly audit: UserAudit;
}

export interface UserRow {
  readonly name: string;
  readonly login: string;
  readonly email: string;
  readonly status: string;
  readonly createdBy: string;
  readonly updatedBy: string;
  readonly createdAt: string;
  readonly updatedAt: string;
}

export type SortColumn = 'login' | 'status' | 'created' | 'updated';

export type TextFilterColumn = 'name' | 'login' | 'email' | 'createdBy' | 'updatedBy';

export type SortDirection = 'asc' | 'desc';

export interface AbsoluteTimeParts {
  readonly date: string;
  readonly time: string;
  readonly tzLabel: string;
  readonly ianaZone: string;
}
