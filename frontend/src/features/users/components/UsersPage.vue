<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { LoaderCircle, Plus } from '@lucide/vue';
import { fetchAdminUsers } from '../logic/admin-users.api';
import { buildUserRows } from '../logic/users-grid.logic';
import type { UserRow } from '../logic/users-grid.types';
import UsersGrid from './UsersGrid.vue';

const rows = ref<UserRow[]>([]);
const loading = ref(true);

async function loadUsers(): Promise<void> {
  loading.value = true;
  try {
    rows.value = buildUserRows(await fetchAdminUsers());
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadUsers();
});
</script>

<template>
  <div data-testid="users-page" class="flex flex-1 flex-col">
    <div class="mb-6 flex items-center justify-between">
      <h1 class="page-title">Users</h1>
      <button
        data-testid="register-user-button"
        type="button"
        class="inline-flex h-10 items-center gap-2 rounded-md bg-accent px-4 text-sm font-medium text-white hover:bg-accent-hover"
      >
        <Plus :size="18" aria-hidden="true" /> Register user
      </button>
    </div>
    <div v-if="loading" data-testid="users-grid-loading" class="flex h-90 items-center justify-center">
      <LoaderCircle :size="32" class="animate-spin text-accent" aria-hidden="true" />
    </div>
    <UsersGrid v-else :rows="rows" />
  </div>
</template>
