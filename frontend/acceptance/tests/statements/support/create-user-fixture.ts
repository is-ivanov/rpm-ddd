import { JOHN_DOE, type AdminUser, type ExpectedUserRow } from './admin-users-fixture';

// Raw form input the E2E types into the Register user modal for scenario 5.1.
// Single source of truth: the modal fill values AND the newly-created user the
// refreshed grid returns are both derived from this, so the two never drift.
export const NEW_USER_INPUT = {
  firstName: 'Grace',
  middleName: 'Brewster',
  lastName: 'Hopper',
  login: 'g.hopper',
  email: 'g.hopper@rpm.local',
} as const;

// The acting admin who registers the new user — reuses the seed's John Doe actor so it
// matches the authenticated current user and the new row's audit actors abbreviate to "J. Doe".
const REGISTERING_ADMIN = JOHN_DOE;

// Non-round timestamp (test-data realism rule) newer than every SEVERAL_ADMIN_USERS
// entry so the newly-created user sorts first under the backend's createdAt-DESC order.
const NEW_USER_CREATED_AT = '2026-06-30T09:42:18.503Z';

// The newly-created user exactly as GET /api/admin/users returns it on the post-create
// refresh: status PENDING (a freshly registered account awaits activation).
export const NEW_PENDING_USER: AdminUser = {
  userId: '00000000-0000-0000-0000-000000000005',
  name: {
    firstName: NEW_USER_INPUT.firstName,
    middleName: NEW_USER_INPUT.middleName,
    lastName: NEW_USER_INPUT.lastName,
  },
  login: NEW_USER_INPUT.login,
  email: NEW_USER_INPUT.email,
  status: 'PENDING',
  audit: {
    createdAt: NEW_USER_CREATED_AT,
    createdBy: REGISTERING_ADMIN,
    updatedAt: NEW_USER_CREATED_AT,
    updatedBy: REGISTERING_ADMIN,
  },
};

// Expected rendered cells for the new user's row: the full name joins the three name
// parts, and status PENDING renders as the "Pending" badge.
export const NEW_PENDING_USER_ROW: ExpectedUserRow = {
  name: 'Grace Brewster Hopper',
  login: NEW_USER_INPUT.login,
  email: NEW_USER_INPUT.email,
  status: 'Pending',
  createdBy: 'J. Doe',
  updatedBy: 'J. Doe',
};
