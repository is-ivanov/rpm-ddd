import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import HomePage from '@/features/home/components/HomePage.vue';
import LoginPage from '@/features/auth/components/LoginPage.vue';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomePage,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage,
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
