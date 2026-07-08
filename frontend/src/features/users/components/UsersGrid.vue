<script setup lang="ts">
import { computed, reactive, ref, type Component } from 'vue';
import { ArrowDown, ArrowUp, ChevronsUpDown } from '@lucide/vue';
import {
  filterRowsByColumns,
  filterRowsByDateRange,
  filterRowsByStatuses,
  sortUserRows,
} from '../logic/users-grid.logic';
import { COLUMNS, type Column } from '../logic/users-grid.columns';
import type {
  DateFilterColumn,
  DateRange,
  SortColumn,
  SortDirection,
  TextFilterColumn,
  UserRow,
} from '../logic/users-grid.types';
import TimeCell from './TimeCell.vue';
import UsersStatusFilter from './UsersStatusFilter.vue';
import UsersDateRangeFilter from './UsersDateRangeFilter.vue';

const props = defineProps<{ rows: readonly UserRow[]; viewerTimeZone: string }>();

const now = new Date();

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

const filters = reactive<Record<TextFilterColumn, string>>({
  name: '',
  login: '',
  email: '',
  createdBy: '',
  updatedBy: '',
});
const statuses = ref<string[]>([]);
const dateRanges = reactive<Record<DateFilterColumn, DateRange>>({
  created: { from: '', to: '' },
  updated: { from: '', to: '' },
});
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
  const textFiltered = filterRowsByColumns([...props.rows], filters);
  const statusFiltered = filterRowsByStatuses(textFiltered, statuses.value);
  const createdFiltered = filterRowsByDateRange(
    statusFiltered,
    'created',
    dateRanges.created.from,
    dateRanges.created.to,
  );
  const filtered = filterRowsByDateRange(createdFiltered, 'updated', dateRanges.updated.from, dateRanges.updated.to);
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
            <UsersStatusFilter v-if="col.statusFilter" v-model="statuses" />
            <UsersDateRangeFilter
              v-else-if="col.dateFilter"
              v-model:from="dateRanges[col.dateFilter].from"
              v-model:to="dateRanges[col.dateFilter].to"
              :column="col.dateFilter"
              :label="col.label"
            />
            <input
              v-else-if="col.filterKey"
              v-model="filters[col.filterKey]"
              :data-testid="col.filterTestId"
              :aria-label="`Filter by ${col.label}`"
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
          <TimeCell
            :iso="row.createdAt"
            :now="now"
            :time-zone="viewerTimeZone"
            cell-test-id="users-cell-created"
            tooltip-test-id="users-created-tooltip"
          />
          <td data-testid="users-cell-created-by" class="grid-cell">{{ row.createdBy }}</td>
          <TimeCell
            :iso="row.updatedAt"
            :now="now"
            :time-zone="viewerTimeZone"
            cell-test-id="users-cell-updated"
            tooltip-test-id="users-updated-tooltip"
          />
          <td data-testid="users-cell-updated-by" class="grid-cell">{{ row.updatedBy }}</td>
        </tr>
        <tr v-if="displayedRows.length === 0">
          <td
            :colspan="COLUMNS.length"
            data-testid="users-grid-empty"
            class="px-4 py-12 text-center text-sm text-muted"
          >
            No users match your filters.
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
