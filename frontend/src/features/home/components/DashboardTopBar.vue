<script setup lang="ts">
import AppVersion from './AppVersion.vue';
import UserMenu from './UserMenu.vue';
</script>

<template>
  <header class="flex h-14 items-center justify-between border-b border-line bg-white px-6">
    <div data-testid="topbar-logo" class="text-xl font-bold text-accent">RPM</div>
    <div class="flex items-center gap-2">
      <AppVersion />
      <UserMenu />
    </div>
  </header>
</template>
