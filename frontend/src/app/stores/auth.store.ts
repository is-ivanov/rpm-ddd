import { defineStore } from 'pinia';
import type { AuthenticatedUser } from '@/features/home/logic/types';
import { buildDashboardUser, type DashboardUser } from '@/features/home/logic/dashboard-user.logic';

interface AuthState {
  currentUser: AuthenticatedUser | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    currentUser: null,
  }),
  getters: {
    isAuthenticated: (state): boolean => state.currentUser !== null,
    dashboardUser: (state): DashboardUser | null =>
      state.currentUser === null ? null : buildDashboardUser(state.currentUser),
  },
  actions: {
    async loadMe(): Promise<void> {
      await Promise.reject(new Error('Not implemented'));
    },
    reset(): void {
      throw new Error('Not implemented');
    },
  },
});
