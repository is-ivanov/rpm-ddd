<script setup lang="ts">
import { computed, ref } from 'vue';
import { Check, ChevronDown } from '@lucide/vue';
import { STATUS_FILTER_OPTIONS } from '../logic/users-grid.logic';

const selected = defineModel<string[]>({ required: true });

const open = ref(false);

const triggerLabel = computed(() =>
  selected.value.length === 0 ? 'All statuses' : `${selected.value.length} selected`,
);

function toggleOpen(): void {
  open.value = !open.value;
}

function isSelected(status: string): boolean {
  return selected.value.includes(status);
}

function toggleStatus(status: string): void {
  selected.value = isSelected(status)
    ? selected.value.filter((candidate) => candidate !== status)
    : [...selected.value, status];
}
</script>

<template>
  <div class="relative" @keydown.esc="open = false">
    <div
      data-testid="users-filter-status"
      class="filter-control filter-control-between"
      role="button"
      tabindex="0"
      @click="toggleOpen"
      @keydown.enter.prevent="toggleOpen"
      @keydown.space.prevent="toggleOpen"
    >
      <span>{{ triggerLabel }}</span>
      <ChevronDown :size="14" class="shrink-0" aria-hidden="true" />
    </div>
    <div v-if="open" class="dropdown-panel status-filter-panel">
      <button
        v-for="status in STATUS_FILTER_OPTIONS"
        :key="status"
        data-testid="users-filter-status-option"
        type="button"
        class="status-filter-option"
        :aria-pressed="isSelected(status)"
        @click="toggleStatus(status)"
      >
        <Check
          :size="14"
          class="shrink-0"
          :class="isSelected(status) ? 'text-accent' : 'invisible'"
          aria-hidden="true"
        />
        {{ status }}
      </button>
    </div>
  </div>
</template>
