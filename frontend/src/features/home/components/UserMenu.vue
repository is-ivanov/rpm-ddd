<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { ChevronDown, ChevronUp, LogOut } from '@lucide/vue';
import { useAuthStore } from '@/app/stores/auth.store';

const router = useRouter();
const store = useAuthStore();
const { dashboardUser } = storeToRefs(store);

const open = ref(false);
const loggingOut = ref(false);

function toggleMenu(): void {
  open.value = !open.value;
}

async function handleLogout(): Promise<void> {
  loggingOut.value = true;
  try {
    await store.logout();
    await router.push('/');
  } finally {
    loggingOut.value = false;
  }
}
</script>

<template>
  <div class="relative">
    <div
      class="flex cursor-pointer items-center gap-2 rounded-md px-2 py-1 transition-colors"
      :class="open ? 'bg-surface' : 'hover:bg-surface'"
      @click="toggleMenu"
    >
      <div
        data-testid="user-avatar"
        class="flex h-8 w-8 items-center justify-center rounded-full bg-accent-surface text-xs font-semibold text-accent"
      >
        {{ dashboardUser?.initials }}
      </div>
      <span data-testid="user-name" class="text-sm font-medium text-ink">{{ dashboardUser?.displayName }}</span>
      <component :is="open ? ChevronUp : ChevronDown" :size="16" class="text-muted" aria-hidden="true" />
    </div>

    <div v-if="open" data-testid="user-menu" class="dropdown-panel dropdown-anchor min-w-55">
      <div class="p-3">
        <div data-testid="user-menu-name" class="text-sm font-semibold text-ink">{{ dashboardUser?.displayName }}</div>
        <div data-testid="user-menu-email" class="mt-0.5 text-[13px] text-muted">{{ dashboardUser?.email }}</div>
      </div>
      <div class="h-px bg-line" />
      <button
        data-testid="user-menu-logout"
        type="button"
        :disabled="loggingOut"
        class="flex h-10 w-full cursor-pointer items-center gap-3 px-3 text-sm text-ink hover:bg-surface disabled:cursor-not-allowed disabled:opacity-60"
        @click="handleLogout"
      >
        <LogOut :size="18" class="text-muted" aria-hidden="true" />
        Log out
      </button>
    </div>
  </div>
</template>
