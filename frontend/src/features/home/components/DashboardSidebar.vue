<script setup lang="ts">
import { LayoutDashboard, Users } from '@lucide/vue';
import { useRoute } from 'vue-router';

const props = defineProps<{ collapsed: boolean }>();

const route = useRoute();

function navItemClasses(routeName: string, expandedIndent: string): string[] {
  const classes = ['nav-item'];
  if (route.name === routeName) {
    classes.push('nav-item-active');
  }
  classes.push(props.collapsed ? 'nav-item-rail' : expandedIndent);
  return classes;
}
</script>

<template>
  <aside
    data-testid="dashboard-sidebar"
    :data-collapsed="String(collapsed)"
    class="flex shrink-0 flex-col bg-sidebar py-4"
    :class="collapsed ? 'w-16' : 'w-60'"
  >
    <RouterLink
      data-testid="home-nav-item"
      to="/"
      :class="navItemClasses('home', '')"
      :title="collapsed ? 'Home' : undefined"
    >
      <LayoutDashboard :size="20" aria-hidden="true" />
      <span v-if="!collapsed">Home</span>
    </RouterLink>

    <div v-if="collapsed" class="rail-divider"></div>
    <div v-else data-testid="admin-center-group" class="nav-group-label">Admin Center</div>

    <RouterLink
      data-testid="users-nav-item"
      to="/users"
      :class="navItemClasses('users', 'pl-8')"
      :title="collapsed ? 'Users' : undefined"
    >
      <Users :size="20" aria-hidden="true" />
      <span v-if="!collapsed">Users</span>
    </RouterLink>
  </aside>
</template>
