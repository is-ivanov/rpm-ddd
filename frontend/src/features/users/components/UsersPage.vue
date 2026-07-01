<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { LoaderCircle, Plus } from '@lucide/vue';
import { useAuthStore } from '@/app/stores/auth.store';
import { fetchAdminUsers } from '../logic/admin-users.api';
import { buildUserRows } from '../logic/users-grid.logic';
import type { UserRow } from '../logic/users-grid.types';
import UsersGrid from './UsersGrid.vue';
import UsersGridError from './UsersGridError.vue';
import RegisterUserModal from './RegisterUserModal.vue';

const auth = useAuthStore();
const viewerTimeZone = computed(() => auth.currentUser?.timeZone ?? 'UTC');

const rows = ref<UserRow[]>([]);
const loading = ref(true);
const error = ref(false);
const modalOpen = ref(false);

async function loadUsers(): Promise<void> {
  loading.value = true;
  error.value = false;
  try {
    rows.value = buildUserRows(await fetchAdminUsers());
  } catch {
    error.value = true;
  } finally {
    loading.value = false;
  }
}

function onUserCreated(): void {
  modalOpen.value = false;
  void loadUsers();
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
        @click="modalOpen = true"
      >
        <Plus :size="18" aria-hidden="true" /> Register user
      </button>
    </div>
    <div v-if="loading" data-testid="users-grid-loading" class="flex h-90 items-center justify-center">
      <LoaderCircle :size="32" class="animate-spin text-accent" aria-hidden="true" />
    </div>
    <UsersGridError v-else-if="error" @retry="loadUsers" />
    <UsersGrid v-else :rows="rows" :viewer-time-zone="viewerTimeZone" />
    <RegisterUserModal v-if="modalOpen" @close="modalOpen = false" @created="onUserCreated" />
  </div>
</template>
