<script setup lang="ts">
import { computed, ref } from 'vue';
import { filterRowsByFullName } from '../logic/users-grid.logic';
import type { UserRow } from '../logic/users-grid.types';

const props = defineProps<{ rows: readonly UserRow[] }>();

interface Column {
  readonly testId: string;
  readonly label: string;
  readonly center?: boolean;
  readonly filterTestId?: string;
}

const COLUMNS: readonly Column[] = [
  { testId: 'users-grid-header-name', label: 'Full name', filterTestId: 'users-filter-name' },
  { testId: 'users-grid-header-login', label: 'Login' },
  { testId: 'users-grid-header-email', label: 'Email' },
  { testId: 'users-grid-header-status', label: 'Status', center: true },
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

const nameFilter = ref('');

const displayedRows = computed(() => filterRowsByFullName([...props.rows], nameFilter.value));
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
            :class="{ 'text-center': col.center }"
          >
            {{ col.label }}
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
