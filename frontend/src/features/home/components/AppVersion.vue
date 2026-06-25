<script setup lang="ts">
import { ref } from 'vue';
import { CircleHelp, LoaderCircle } from '@lucide/vue';
import { getAppInfo } from '@/app/logic/app-version.api';
import { buildAppVersion } from '@/app/logic/app-version.logic';
import type { AppVersion } from '@/app/logic/app-version.types';

const open = ref(false);
const loading = ref(false);
const appVersion = ref<AppVersion | null>(null);

async function togglePopover(): Promise<void> {
  open.value = !open.value;
  if (open.value && appVersion.value === null && !loading.value) {
    await loadAppVersion();
  }
}

async function loadAppVersion(): Promise<void> {
  loading.value = true;
  try {
    appVersion.value = buildAppVersion(await getAppInfo());
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="relative">
    <button
      data-testid="app-version-trigger"
      type="button"
      aria-label="Show app version"
      class="icon-button rounded-md p-1.5 text-muted transition-colors hover:bg-surface hover:text-ink"
      @click="togglePopover"
    >
      <CircleHelp :size="18" aria-hidden="true" />
    </button>

    <div v-if="open" data-testid="app-version-popover" class="dropdown-panel dropdown-anchor min-w-60 p-3">
      <div v-if="loading" data-testid="app-version-loading" class="flex items-center gap-2 text-sm text-muted">
        <LoaderCircle :size="16" class="animate-spin text-accent" aria-hidden="true" />
        Loading…
      </div>
      <dl v-else-if="appVersion" class="grid grid-cols-[auto_1fr] gap-x-6 gap-y-1.5 text-sm">
        <dt class="text-muted">Version</dt>
        <dd data-testid="app-version-number" class="text-right font-medium text-ink">{{ appVersion.version }}</dd>
        <dt class="text-muted">Commit</dt>
        <dd data-testid="app-version-commit" class="text-right font-mono text-ink">{{ appVersion.commit }}</dd>
        <dt class="text-muted">Built</dt>
        <dd data-testid="app-version-build-time" class="text-right text-ink">{{ appVersion.buildTime }}</dd>
      </dl>
    </div>
  </div>
</template>
