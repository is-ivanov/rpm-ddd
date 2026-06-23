<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/app/stores/auth.store';
import AppLoading from '@/app/components/AppLoading.vue';
import WelcomeView from './WelcomeView.vue';
import DashboardShell from './DashboardShell.vue';

const auth = useAuthStore();
const { dashboardUser } = storeToRefs(auth);
const loading = ref(true);

onMounted(loadCurrentUser);

async function loadCurrentUser(): Promise<void> {
  try {
    await auth.loadMe();
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <AppLoading v-if="loading" />
  <DashboardShell v-else-if="dashboardUser" />
  <WelcomeView v-else />
</template>
