<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchCurrentUser } from '../logic/current-user.api';
import { buildDashboardUser, type DashboardUser } from '../logic/dashboard-user.logic';
import AppLoading from '@/app/components/AppLoading.vue';
import WelcomeView from './WelcomeView.vue';
import DashboardShell from './DashboardShell.vue';

const loading = ref(true);
const dashboardUser = ref<DashboardUser | null>(null);

onMounted(loadCurrentUser);

async function loadCurrentUser(): Promise<void> {
  try {
    const result = await fetchCurrentUser();
    if (result.authenticated) {
      dashboardUser.value = buildDashboardUser(result.user);
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AppLoading v-if="loading" />
  <DashboardShell v-else-if="dashboardUser" :user="dashboardUser" />
  <WelcomeView v-else />
</template>
