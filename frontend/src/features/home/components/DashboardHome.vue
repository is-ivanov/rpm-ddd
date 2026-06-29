<script setup lang="ts">
import { LayoutDashboard } from '@lucide/vue';
</script>

<template>
  <h1 data-testid="page-title" class="page-title">Home</h1>
  <div data-testid="dashboard-placeholder" class="flex flex-1 flex-col items-center justify-center gap-3 text-center">
    <LayoutDashboard :size="48" class="text-placeholder" aria-hidden="true" />
    <div class="text-lg font-semibold text-ink">Dashboard</div>
    <p class="max-w-90 text-sm leading-normal text-muted">
      Patient, order, and alert data will appear here as modules are connected.
    </p>
  </div>
</template>
