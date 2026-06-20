<script setup lang="ts">
import { AlertTriangle, XCircle } from '@lucide/vue';

defineProps<{
  message: string;
  requiresActivation: boolean;
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
  </div>
</template>
