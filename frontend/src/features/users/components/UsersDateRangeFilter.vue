<script setup lang="ts">
import { computed } from 'vue';
import { Calendar } from '@lucide/vue';
import { useAnchoredPanel } from '@/app/logic/use-anchored-panel';
import type { DateFilterColumn } from '../logic/users-grid.types';

const props = defineProps<{ column: DateFilterColumn; label: string }>();

const from = defineModel<string>('from', { required: true });
const to = defineModel<string>('to', { required: true });

const { open, anchor, position, toggle: toggleOpen } = useAnchoredPanel();

const triggerLabel = computed(() =>
  from.value === '' && to.value === '' ? 'from – to' : `${from.value || '…'} – ${to.value || '…'}`,
);

const rangeTestId = computed(() => `users-filter-${props.column}-range`);
const fromTestId = computed(() => `users-filter-${props.column}-from`);
const toTestId = computed(() => `users-filter-${props.column}-to`);
</script>

<template>
  <div class="relative" @keydown.esc="open = false">
    <button ref="anchor" :data-testid="rangeTestId" type="button" class="filter-control" @click="toggleOpen">
      <Calendar :size="14" class="shrink-0" aria-hidden="true" />
      <span>{{ triggerLabel }}</span>
    </button>
    <Teleport to="body">
      <div
        v-if="open"
        class="dropdown-panel date-range-panel"
        :style="{ top: `${position.top}px`, left: `${position.left}px` }"
      >
        <label class="date-range-field">
          From
          <input
            v-model="from"
            :data-testid="fromTestId"
            :aria-label="`${label} from`"
            type="date"
            class="filter-input"
          />
        </label>
        <label class="date-range-field">
          To
          <input v-model="to" :data-testid="toTestId" :aria-label="`${label} to`" type="date" class="filter-input" />
        </label>
      </div>
    </Teleport>
  </div>
</template>
