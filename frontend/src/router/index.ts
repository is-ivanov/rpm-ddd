import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import HomePage from '@/features/home/components/HomePage.vue';
import DashboardHome from '@/features/home/components/DashboardHome.vue';
import UsersPage from '@/features/users/components/UsersPage.vue';
import LoginPage from '@/features/auth/components/LoginPage.vue';
import ActivationPage from '@/features/auth/components/ActivationPage.vue';
import NotFoundPage from '@/features/not-found/components/NotFoundPage.vue';
import { useAuthStore } from '@/app/stores/auth.store';
import { LOGIN_PATH, shouldRedirectToLogin } from '@/app/logic/unauthorized-redirect.logic';

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean;
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: HomePage,
    children: [
      {
        path: '',
        name: 'home',
        component: DashboardHome,
      },
      {
        path: 'users',
        name: 'users',
        component: UsersPage,
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
  },
  {
    path: '/activate',
    name: 'activate',
    component: ActivationPage,
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundPage,
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const requiresAuth = to.meta.requiresAuth === true;
  const { isAuthenticated } = useAuthStore();
  return shouldRedirectToLogin(requiresAuth, isAuthenticated) ? LOGIN_PATH : true;
});
