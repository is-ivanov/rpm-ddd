<script setup lang="ts">
import { RefreshCw, XCircle } from '@lucide/vue';

defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div data-testid="users-grid-error" class="table-card flex flex-col items-center gap-4 px-6 py-16 text-center">
    <XCircle :size="32" class="text-danger" aria-hidden="true" />
    <p data-testid="users-grid-error-message" class="text-sm text-muted">
      Something went wrong while loading users. Please try again.
    </p>
    <button
      type="button"
      data-testid="users-grid-error-retry"
      class="inline-flex h-10 items-center gap-2 rounded-md bg-accent px-4 text-sm font-medium text-white hover:bg-accent-hover"
      @click="$emit('retry')"
    >
      <RefreshCw :size="16" aria-hidden="true" /> Retry
    </button>
  </div>
</template>
