<script setup lang="ts">
import { computed } from 'vue';
import { Check, ChevronDown } from '@lucide/vue';
import { useAnchoredPanel } from '@/app/logic/use-anchored-panel';
import { STATUS_FILTER_OPTIONS } from '../logic/users-grid.logic';

const selected = defineModel<string[]>({ required: true });

const { open, anchor, position, toggle: toggleOpen } = useAnchoredPanel();

const triggerLabel = computed(() =>
  selected.value.length === 0 ? 'All statuses' : `${selected.value.length} selected`,
);

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
    <button
      ref="anchor"
      data-testid="users-filter-status"
      type="button"
      class="filter-control filter-control-between"
      @click="toggleOpen"
    >
      <span>{{ triggerLabel }}</span>
      <ChevronDown :size="14" class="shrink-0" aria-hidden="true" />
    </button>
    <Teleport to="body">
      <div
        v-if="open"
        class="dropdown-panel status-filter-panel"
        :style="{ top: `${position.top}px`, left: `${position.left}px` }"
      >
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
    </Teleport>
  </div>
</template>
