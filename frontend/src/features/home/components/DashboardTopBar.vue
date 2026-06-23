<script setup lang="ts">
import UserMenu from './UserMenu.vue';

defineProps<{
  displayName: string;
  initials: string;
  email: string;
}>();
</script>

<template>
  <header class="flex h-14 items-center justify-between border-b border-line bg-white px-6">
    <div data-testid="topbar-logo" class="text-xl font-bold text-accent">RPM</div>
    <UserMenu :display-name="displayName" :initials="initials" :email="email" />
  </header>
</template>
