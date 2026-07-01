const ME_PATH = '/api/auth/me';

/** The GET /api/auth/me success wire body, before the client schema strips server-only keys. */
export interface CurrentUserResponseBody {
  readonly userId: string;
  readonly login: string;
  readonly email: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly status: string;
  readonly roles: readonly string[];
  readonly timeZone: string;
}

const CURRENT_USER_RESPONSE_DEFAULTS: CurrentUserResponseBody = {
  userId: '11111111-1111-1111-1111-111111111111',
  login: 'jdoe',
  email: 'j.doe@rpm.local',
  firstName: 'John',
  lastName: 'Doe',
  status: 'ACTIVE',
  roles: [],
  timeZone: 'Europe/Berlin',
};

/** Builds the canonical GET /api/auth/me success body for the authenticated viewer (John Doe). */
export function aCurrentUserResponse(overrides: Partial<CurrentUserResponseBody> = {}): CurrentUserResponseBody {
  return { ...CURRENT_USER_RESPONSE_DEFAULTS, ...overrides };
}

/** The RFC 9457 problem body GET /api/auth/me returns when the caller is unauthenticated. */
export interface UnauthenticatedProblem {
  readonly type: string;
  readonly title: string;
  readonly status: number;
  readonly detail: string;
  readonly instance: string;
}

const UNAUTHENTICATED_PROBLEM_DEFAULTS: UnauthenticatedProblem = {
  type: 'https://www.rpm-ddd.my/problem/authentication-failed',
  title: 'Unauthorized',
  status: 401,
  detail: 'Full authentication is required to access this resource.',
  instance: ME_PATH,
};

/** Builds the canonical unauthenticated problem-detail body for GET /api/auth/me. */
export function anUnauthenticatedProblem(overrides: Partial<UnauthenticatedProblem> = {}): UnauthenticatedProblem {
  return { ...UNAUTHENTICATED_PROBLEM_DEFAULTS, ...overrides };
}
