<script setup lang="ts">
import { LayoutDashboard, Users } from '@lucide/vue';
import { useRoute } from 'vue-router';
import DashboardTopBar from './DashboardTopBar.vue';

const route = useRoute();

function navItemClasses(routeName: string): string[] {
  const classes = ['nav-item'];
  if (route.name === routeName) {
    classes.push('nav-item-active');
  }
  return classes;
}
</script>

<template>
  <div data-testid="dashboard-shell" class="flex min-h-screen flex-col bg-surface font-sans">
    <DashboardTopBar />
    <div class="flex min-h-0 flex-1">
      <aside data-testid="dashboard-sidebar" class="flex w-60 shrink-0 flex-col bg-sidebar py-4">
        <RouterLink data-testid="home-nav-item" to="/" :class="navItemClasses('home')">
          <LayoutDashboard :size="20" aria-hidden="true" /> Home
        </RouterLink>
        <div data-testid="admin-center-group" class="nav-group-label">Admin Center</div>
        <RouterLink data-testid="users-nav-item" to="/users" :class="[...navItemClasses('users'), 'pl-8']">
          <Users :size="20" aria-hidden="true" /> Users
        </RouterLink>
      </aside>
      <main class="flex flex-1 flex-col p-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
