import { describe, expect, it } from 'vitest';
import { mount, RouterLinkStub } from '@vue/test-utils';
import HomePage from '../components/HomePage.vue';

describe('HomePage', () => {
  it('renders the welcome logo', () => {
    const wrapper = mount(HomePage, {
      global: { stubs: { RouterLink: RouterLinkStub } },
    });

    expect(wrapper.get('[data-testid="welcome-logo"]').text()).toBe('RPM');
  });
});
