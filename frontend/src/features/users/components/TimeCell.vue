<script setup lang="ts">
import { computed, ref } from 'vue';
import { toAbsoluteTooltipParts, toRelativeTimeLabel } from '../logic/users-grid.logic';

const props = defineProps<{
  iso: string;
  now: Date;
  timeZone: string;
  cellTestId: string;
  tooltipTestId: string;
}>();

const hovered = ref(false);
const anchor = ref<HTMLElement | null>(null);
const position = ref({ top: 0, left: 0 });

const relativeLabel = computed(() => toRelativeTimeLabel(props.iso, props.now));
const tooltipParts = computed(() => toAbsoluteTooltipParts(props.iso, props.timeZone));

function show(): void {
  const rect = anchor.value?.getBoundingClientRect();
  if (rect) {
    position.value = { top: rect.bottom + 6, left: rect.left };
  }
  hovered.value = true;
}

function hide(): void {
  hovered.value = false;
}
</script>

<template>
  <td :data-testid="cellTestId" class="grid-cell">
    <span ref="anchor" class="ts-rel" @mouseenter="show" @mouseleave="hide">{{ relativeLabel }}</span>
    <Teleport to="body">
      <div
        v-if="hovered"
        :data-testid="tooltipTestId"
        role="tooltip"
        class="ts-tooltip"
        :style="{ top: `${position.top}px`, left: `${position.left}px` }"
      >
        <div class="ts-tooltip-time">
          <span data-testid="tooltip-date">{{ tooltipParts.date }}</span>
          <span data-testid="tooltip-time">{{ tooltipParts.time }}</span>
        </div>
        <div class="ts-tooltip-zone">
          <span data-testid="tooltip-tz-label">{{ tooltipParts.tzLabel }}</span>
          <span aria-hidden="true">·</span>
          <span data-testid="tooltip-tz-id">{{ tooltipParts.ianaZone }}</span>
        </div>
      </div>
    </Teleport>
  </td>
</template>
