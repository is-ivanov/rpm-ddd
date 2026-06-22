<script setup lang="ts">
import { ChevronDown } from '@lucide/vue';

defineProps<{
  displayName: string;
  initials: string;
}>();
</script>

<template>
  <header class="flex h-14 items-center justify-between border-b border-line bg-white px-6">
    <div data-testid="topbar-logo" class="text-xl font-bold text-accent">RPM</div>
    <div class="flex cursor-pointer items-center gap-2 rounded-md px-2 py-1 transition-colors hover:bg-surface">
      <div
        data-testid="user-avatar"
        class="flex h-8 w-8 items-center justify-center rounded-full bg-accent-surface text-xs font-semibold text-accent"
      >
        {{ initials }}
      </div>
      <span data-testid="user-name" class="text-sm font-medium text-ink">{{ displayName }}</span>
      <ChevronDown :size="16" class="text-muted" aria-hidden="true" />
    </div>
  </header>
</template>
