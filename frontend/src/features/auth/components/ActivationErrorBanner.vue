<script setup lang="ts">
import { XCircle } from '@lucide/vue';

defineProps<{
  message: string;
}>();
</script>

<template>
  <div
    data-testid="activation-server-error"
    class="mb-4 flex items-start gap-2 rounded-md bg-[#fff5f5] px-3 py-2.5 text-[13px] leading-[1.4] text-[#fa5252]"
  >
    <XCircle :size="16" class="mt-px shrink-0" />
    <span>{{ message }}</span>
  </div>
</template>
