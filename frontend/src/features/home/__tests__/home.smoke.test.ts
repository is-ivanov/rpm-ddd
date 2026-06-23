import { describe, expect, it } from 'vitest';
import { flushPromises, mount, RouterLinkStub } from '@vue/test-utils';
import { createPinia } from 'pinia';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import HomePage from '../components/HomePage.vue';

const BASE = import.meta.env.VITE_API_URL;
const ME_PATH = '/api/auth/me';

function stubUnauthenticated(): void {
  server.use(
    http.get(`${BASE}${ME_PATH}`, () =>
      HttpResponse.json(
        { type: 'about:blank', title: 'Unauthorized', status: 401 },
        { status: 401, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

function stubAuthenticated(): void {
  server.use(
    http.get(`${BASE}${ME_PATH}`, () =>
      HttpResponse.json({
        login: 'ipetrov',
        email: 'i.petrov@rpm.local',
        firstName: 'Иван',
        lastName: 'Петров',
      }),
    ),
  );
}

function mountHomePage() {
  return mount(HomePage, {
    global: { plugins: [createPinia()], stubs: { RouterLink: RouterLinkStub } },
  });
}

describe('HomePage', () => {
  it('renders the welcome view when the visitor is unauthenticated', async () => {
    stubUnauthenticated();

    const wrapper = mountHomePage();
    await flushPromises();

    expect(wrapper.get('[data-testid="welcome-logo"]').text()).toBe('RPM');
    expect(wrapper.find('[data-testid="dashboard-shell"]').exists()).toBe(false);
  });

  it('renders the dashboard shell for the current user when authenticated', async () => {
    stubAuthenticated();

    const wrapper = mountHomePage();
    await flushPromises();

    expect(wrapper.get('[data-testid="user-name"]').text()).toBe('Иван Петров');
    expect(wrapper.get('[data-testid="user-avatar"]').text()).toBe('ИП');
    expect(wrapper.get('[data-testid="page-title"]').text()).toBe('Главная');
    expect(wrapper.find('[data-testid="welcome-logo"]').exists()).toBe(false);
  });
});
