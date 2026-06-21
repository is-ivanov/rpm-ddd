<script setup lang="ts">
import { AlertTriangle, X, XCircle } from '@lucide/vue';

defineProps<{
  message: string;
  requiresActivation: boolean;
}>();

defineEmits<{
  dismiss: [];
}>();
</script>

<template>
  <div data-testid="error-banner" class="error-banner">
    <AlertTriangle v-if="requiresActivation" :size="16" class="mt-px shrink-0" />
    <XCircle v-else :size="16" class="mt-px shrink-0" />
    <span>
      {{ message }}
      <a v-if="requiresActivation" data-testid="activation-link" href="#" class="font-medium text-danger underline">
        Request a new activation email
      </a>
    </span>
    <button
      type="button"
      data-testid="error-banner-dismiss"
      aria-label="Dismiss error"
      class="icon-button ml-auto shrink-0 p-0.5 text-danger hover:opacity-70"
      @click="$emit('dismiss')"
    >
      <X :size="16" />
    </button>
  </div>
</template>
