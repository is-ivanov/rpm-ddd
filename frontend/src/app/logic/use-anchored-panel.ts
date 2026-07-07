import { ref, type Ref } from 'vue';

export interface PanelPosition {
  readonly top: number;
  readonly left: number;
}

export interface AnchoredPanel {
  readonly open: Ref<boolean>;
  readonly anchor: Ref<HTMLElement | null>;
  readonly position: Ref<PanelPosition>;
  readonly toggle: () => void;
}

/**
 * Popover state for a filter trigger whose panel is Teleported to the body: tracks open state and
 * computes the panel's fixed position from the trigger's bounding rect when opening. Shared by the
 * grid filter popovers so a `absolute` panel is never clipped by the table-card's overflow.
 */
export function useAnchoredPanel(offsetY = 4): AnchoredPanel {
  const open = ref(false);
  const anchor = ref<HTMLElement | null>(null);
  const position = ref<PanelPosition>({ top: 0, left: 0 });

  function toggle(): void {
    if (!open.value) {
      const rect = anchor.value?.getBoundingClientRect();
      if (rect) {
        position.value = { top: rect.bottom + offsetY, left: rect.left };
      }
    }
    open.value = !open.value;
  }

  return { open, anchor, position, toggle };
}
