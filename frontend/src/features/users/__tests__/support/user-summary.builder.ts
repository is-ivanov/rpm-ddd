import type { PersonName, UserSummaryResponse } from '../../logic/users-grid.types';

export const JOHN_DOE: PersonName = { firstName: 'John', middleName: 'Robert', lastName: 'Doe' };
export const SARAH_CONNOR: PersonName = { firstName: 'Sarah', middleName: 'Jane', lastName: 'Connor' };
export const MICHAEL_SCOTT: PersonName = { firstName: 'Michael', middleName: null, lastName: 'Scott' };
export const EMILY_CARTER: PersonName = { firstName: 'Emily', middleName: null, lastName: 'Carter' };
export const DAVID_LEE: PersonName = { firstName: 'David', middleName: null, lastName: 'Lee' };
export const SYSTEM_ACTOR: PersonName = { firstName: 'System', middleName: null, lastName: '' };

export function userWith(overrides: Partial<UserSummaryResponse>): UserSummaryResponse {
  return {
    userId: '00000000-0000-0000-0000-000000000001',
    name: SARAH_CONNOR,
    login: 's.connor',
    email: 's.connor@rpm.local',
    status: 'ACTIVE',
    audit: {
      createdAt: '2026-06-22T14:30:51.217Z',
      createdBy: JOHN_DOE,
      updatedAt: '2026-06-24T08:11:42.905Z',
      updatedBy: SARAH_CONNOR,
    },
    ...overrides,
  };
}
