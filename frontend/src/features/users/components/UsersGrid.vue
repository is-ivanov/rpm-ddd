<script setup lang="ts">
import { computed, ref, type Component } from 'vue';
import { ArrowDown, ArrowUp, ChevronsUpDown } from '@lucide/vue';
import { filterRowsByFullName, sortUserRows } from '../logic/users-grid.logic';
import type { SortColumn, SortDirection, UserRow } from '../logic/users-grid.types';

const props = defineProps<{ rows: readonly UserRow[] }>();

interface Column {
  readonly testId: string;
  readonly label: string;
  readonly center?: boolean;
  readonly filterTestId?: string;
  readonly sortKey?: SortColumn;
}

const COLUMNS: readonly Column[] = [
  { testId: 'users-grid-header-name', label: 'Full name', filterTestId: 'users-filter-name' },
  { testId: 'users-grid-header-login', label: 'Login', sortKey: 'login' },
  { testId: 'users-grid-header-email', label: 'Email' },
  { testId: 'users-grid-header-status', label: 'Status', center: true, sortKey: 'status' },
  { testId: 'users-grid-header-created', label: 'Created' },
  { testId: 'users-grid-header-created-by', label: 'Created by' },
  { testId: 'users-grid-header-updated', label: 'Updated' },
  { testId: 'users-grid-header-updated-by', label: 'Updated by' },
];

const STATUS_BADGE_CLASS: Record<string, string> = {
  Active: 'status-active',
  Pending: 'status-pending',
  Locked: 'status-locked',
  Inactive: 'status-inactive',
};

function statusBadgeClass(status: string): string {
  return STATUS_BADGE_CLASS[status] ?? 'status-inactive';
}

interface SortState {
  readonly column: SortColumn;
  readonly direction: SortDirection;
}

const nameFilter = ref('');
const sort = ref<SortState | null>(null);

function onHeaderClick(col: Column): void {
  if (col.sortKey === undefined) {
    return;
  }
  toggleSort(col.sortKey);
}

function toggleSort(column: SortColumn): void {
  const current = sort.value;
  if (current?.column !== column) {
    sort.value = { column, direction: 'asc' };
    return;
  }
  sort.value = { column, direction: current.direction === 'asc' ? 'desc' : 'asc' };
}

function isActiveSort(col: Column): boolean {
  return col.sortKey !== undefined && sort.value?.column === col.sortKey;
}

function sortIconFor(col: Column): Component {
  if (isActiveSort(col) && sort.value?.direction === 'desc') {
    return ArrowDown;
  }
  if (isActiveSort(col)) {
    return ArrowUp;
  }
  return ChevronsUpDown;
}

const displayedRows = computed(() => {
  const filtered = filterRowsByFullName([...props.rows], nameFilter.value);
  if (sort.value === null) {
    return filtered;
  }
  return sortUserRows(filtered, sort.value.column, sort.value.direction);
});
</script>

<template>
  <div data-testid="users-grid" class="table-card">
    <table class="w-full border-collapse">
      <thead>
        <tr>
          <th
            v-for="col in COLUMNS"
            :key="col.testId"
            :data-testid="col.testId"
            class="grid-head-cell"
            :class="[col.center ? 'text-center' : '', col.sortKey ? 'grid-head-cell-sortable' : '']"
            @click="onHeaderClick(col)"
          >
            <span v-if="col.sortKey" class="th-sort" :class="{ 'th-sort-sorted': isActiveSort(col) }">
              {{ col.label }}
              <component :is="sortIconFor(col)" :size="14" class="th-sort-icon" aria-hidden="true" />
            </span>
            <template v-else>{{ col.label }}</template>
          </th>
        </tr>
        <tr>
          <td v-for="col in COLUMNS" :key="col.testId" class="filter-cell">
            <input
              v-if="col.filterTestId"
              v-model="nameFilter"
              :data-testid="col.filterTestId"
              type="text"
              class="filter-input"
              placeholder="contains"
            />
          </td>
        </tr>
      </thead>
      <tbody class="[&>tr:last-child>td]:border-b-0">
        <tr v-for="row in displayedRows" :key="row.login" data-testid="users-grid-row" class="hover:bg-surface">
          <td data-testid="users-cell-name" class="grid-cell">{{ row.name }}</td>
          <td data-testid="users-cell-login" class="grid-cell">{{ row.login }}</td>
          <td data-testid="users-cell-email" class="grid-cell">{{ row.email }}</td>
          <td class="grid-cell text-center">
            <span data-testid="users-status-badge" class="status-badge" :class="statusBadgeClass(row.status)">
              {{ row.status }}
            </span>
          </td>
          <td data-testid="users-cell-created" class="grid-cell text-muted">{{ row.createdAt }}</td>
          <td data-testid="users-cell-created-by" class="grid-cell">{{ row.createdBy }}</td>
          <td data-testid="users-cell-updated" class="grid-cell text-muted">{{ row.updatedAt }}</td>
          <td data-testid="users-cell-updated-by" class="grid-cell">{{ row.updatedBy }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
