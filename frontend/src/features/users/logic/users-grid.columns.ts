import type { DateFilterColumn, SortColumn, TextFilterColumn } from './users-grid.types';

export interface Column {
  readonly testId: string;
  readonly label: string;
  readonly center?: boolean;
  readonly filterTestId?: string;
  readonly filterKey?: TextFilterColumn;
  readonly statusFilter?: boolean;
  readonly dateFilter?: DateFilterColumn;
  readonly sortKey?: SortColumn;
}

export const COLUMNS: readonly Column[] = [
  {
    testId: 'users-grid-header-name',
    label: 'Full name',
    sortKey: 'name',
    filterTestId: 'users-filter-name',
    filterKey: 'name',
  },
  {
    testId: 'users-grid-header-login',
    label: 'Login',
    sortKey: 'login',
    filterTestId: 'users-filter-login',
    filterKey: 'login',
  },
  {
    testId: 'users-grid-header-email',
    label: 'Email',
    sortKey: 'email',
    filterTestId: 'users-filter-email',
    filterKey: 'email',
  },
  { testId: 'users-grid-header-status', label: 'Status', center: true, sortKey: 'status', statusFilter: true },
  { testId: 'users-grid-header-created', label: 'Created', sortKey: 'created', dateFilter: 'created' },
  {
    testId: 'users-grid-header-created-by',
    label: 'Created by',
    sortKey: 'createdBy',
    filterTestId: 'users-filter-created-by',
    filterKey: 'createdBy',
  },
  { testId: 'users-grid-header-updated', label: 'Updated', sortKey: 'updated', dateFilter: 'updated' },
  {
    testId: 'users-grid-header-updated-by',
    label: 'Updated by',
    sortKey: 'updatedBy',
    filterTestId: 'users-filter-updated-by',
    filterKey: 'updatedBy',
  },
];
