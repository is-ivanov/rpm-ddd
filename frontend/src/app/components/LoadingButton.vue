<script setup lang="ts">
import { LoaderCircle } from '@lucide/vue';

withDefaults(
  defineProps<{
    label: string;
    loadingLabel: string;
    loading: boolean;
    disabled?: boolean;
    testId: string;
    loadingTestId: string;
  }>(),
  { disabled: false },
);
</script>

<template>
  <button
    type="submit"
    :data-testid="testId"
    class="btn-primary flex items-center justify-center gap-2"
    :disabled="disabled || loading"
    :aria-busy="loading"
  >
    <LoaderCircle v-if="loading" :data-testid="loadingTestId" :size="16" class="animate-spin" aria-hidden="true" />
    {{ loading ? loadingLabel : label }}
  </button>
</template>
