<script setup lang="ts">
import { watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/app/stores/auth.store';
import { LOGIN_PATH, shouldRedirectOnSessionLoss } from '@/app/logic/unauthorized-redirect.logic';

const authStore = useAuthStore();
const router = useRouter();

watch(
  () => authStore.isAuthenticated,
  (isAuthenticated, wasAuthenticated) => {
    if (shouldRedirectOnSessionLoss(wasAuthenticated, isAuthenticated)) {
      void router.push(LOGIN_PATH);
    }
  },
);
</script>

<template>
  <RouterView />
</template>
