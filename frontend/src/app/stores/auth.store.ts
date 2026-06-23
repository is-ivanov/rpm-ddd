import { defineStore } from 'pinia';
import type { AuthenticatedUser } from '@/features/home/logic/types';
import { buildDashboardUser, type DashboardUser } from '@/features/home/logic/dashboard-user.logic';
import { fetchCurrentUser } from '@/features/home/logic/current-user.api';

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
      const result = await fetchCurrentUser();
      if (result.authenticated) {
        this.currentUser = result.user;
      }
    },
    reset(): void {
      this.currentUser = null;
    },
  },
});
