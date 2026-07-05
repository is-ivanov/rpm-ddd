<script setup lang="ts">
import { ref, watch } from 'vue';
import DashboardSidebar from './DashboardSidebar.vue';
import DashboardTopBar from './DashboardTopBar.vue';
import { SIDEBAR_COLLAPSED_STORAGE_KEY, parseSidebarCollapsedState } from '../logic/sidebar-collapse.logic';

const collapsed = ref(parseSidebarCollapsedState(localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY)));

watch(collapsed, (value) => {
  localStorage.setItem(SIDEBAR_COLLAPSED_STORAGE_KEY, String(value));
});

function toggleCollapsed(): void {
  collapsed.value = !collapsed.value;
}
</script>

<template>
  <div data-testid="dashboard-shell" class="flex min-h-screen flex-col bg-surface font-sans">
    <DashboardTopBar :collapsed="collapsed" @toggle="toggleCollapsed" />
    <div class="flex min-h-0 flex-1">
      <DashboardSidebar :collapsed="collapsed" />
      <main class="flex flex-1 flex-col p-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
