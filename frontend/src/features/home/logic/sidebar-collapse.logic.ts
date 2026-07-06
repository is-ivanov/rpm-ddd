export const SIDEBAR_COLLAPSED_STORAGE_KEY = 'rpm.dashboard.sidebarCollapsed';

export function parseSidebarCollapsedState(raw: string | null): boolean {
  return raw === 'true';
}
