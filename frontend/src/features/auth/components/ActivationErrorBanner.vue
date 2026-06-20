<script setup lang="ts">
import { XCircle } from '@lucide/vue';

defineProps<{
  message: string;
}>();
</script>

<template>
  <div data-testid="activation-server-error" class="error-banner">
    <XCircle :size="16" class="mt-px shrink-0" />
    <span>{{ message }}</span>
  </div>
</template>
