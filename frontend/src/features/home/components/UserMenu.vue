<script setup lang="ts">
import { ref } from 'vue';
import { ChevronDown, ChevronUp, LogOut } from '@lucide/vue';
import { logout } from '@/features/auth/logic/logout.api';

defineProps<{
  displayName: string;
  initials: string;
  email: string;
}>();

const open = ref(false);
const loggingOut = ref(false);

function toggleMenu(): void {
  open.value = !open.value;
}

async function handleLogout(): Promise<void> {
  loggingOut.value = true;
  try {
    await logout();
    window.location.reload();
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
        {{ initials }}
      </div>
      <span data-testid="user-name" class="text-sm font-medium text-ink">{{ displayName }}</span>
      <component :is="open ? ChevronUp : ChevronDown" :size="16" class="text-muted" aria-hidden="true" />
    </div>

    <div v-if="open" data-testid="user-menu" class="dropdown-panel absolute right-0 top-[calc(100%+8px)] min-w-55">
      <div class="p-3">
        <div data-testid="user-menu-name" class="text-sm font-semibold text-ink">{{ displayName }}</div>
        <div data-testid="user-menu-email" class="mt-0.5 text-[13px] text-muted">{{ email }}</div>
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
        Выйти
      </button>
    </div>
  </div>
</template>
