<script setup lang="ts">
import { LoaderCircle } from '@lucide/vue';
</script>

<template>
  <main
    data-testid="app-loading"
    class="flex min-h-screen flex-col items-center justify-center gap-4 bg-surface font-sans"
  >
    <LoaderCircle :size="32" class="animate-spin text-accent" aria-hidden="true" />
    <div class="text-sm text-muted">Loading…</div>
  </main>
</template>
