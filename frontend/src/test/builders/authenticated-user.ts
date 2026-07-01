import type { AuthenticatedUser } from '@/app/logic/current-user.types';

const AUTHENTICATED_USER_DEFAULTS: AuthenticatedUser = {
  login: 'jdoe',
  email: 'j.doe@rpm.local',
  firstName: 'John',
  lastName: 'Doe',
  timeZone: 'Europe/Berlin',
};

/** Builds the canonical authenticated viewer (John Doe), overriding only the fields a test cares about. */
export function anAuthenticatedUser(overrides: Partial<AuthenticatedUser> = {}): AuthenticatedUser {
  return { ...AUTHENTICATED_USER_DEFAULTS, ...overrides };
}
