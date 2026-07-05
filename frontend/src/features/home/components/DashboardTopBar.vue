<script setup lang="ts">
import { PanelLeft, PanelLeftOpen } from '@lucide/vue';
import AppVersion from './AppVersion.vue';
import UserMenu from './UserMenu.vue';

defineProps<{ collapsed: boolean }>();
defineEmits<{ toggle: [] }>();
</script>

<template>
  <header class="flex h-14 items-center justify-between border-b border-line bg-white px-6">
    <div class="flex items-center gap-3">
      <button
        data-testid="sidebar-collapse-toggle"
        type="button"
        class="icon-btn"
        :title="collapsed ? 'Expand menu' : 'Collapse menu'"
        @click="$emit('toggle')"
      >
        <PanelLeftOpen v-if="collapsed" :size="20" aria-hidden="true" />
        <PanelLeft v-else :size="20" aria-hidden="true" />
      </button>
      <div data-testid="topbar-logo" class="text-xl font-bold text-accent">RPM</div>
    </div>
    <div class="flex items-center gap-2">
      <AppVersion />
      <UserMenu />
    </div>
  </header>
</template>
