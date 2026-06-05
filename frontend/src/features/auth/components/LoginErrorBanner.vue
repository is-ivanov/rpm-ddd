<script setup lang="ts">
import { AlertTriangle, XCircle } from '@lucide/vue';

defineProps<{
  message: string;
  requiresActivation: boolean;
}>();
</script>

<template>
  <div
    data-testid="error-banner"
    class="mb-4 flex items-start gap-2 rounded-md bg-[#fff5f5] px-3 py-2.5 text-[13px] leading-[1.4] text-[#fa5252]"
  >
    <AlertTriangle v-if="requiresActivation" :size="16" class="mt-px shrink-0" />
    <XCircle v-else :size="16" class="mt-px shrink-0" />
    <span>
      {{ message }}
      <a v-if="requiresActivation" data-testid="activation-link" href="#" class="font-medium text-[#fa5252] underline">
        Request a new activation email
      </a>
    </span>
  </div>
</template>
