<script setup lang="ts">
import { Plus } from '@lucide/vue';
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
  </div>
</template>
