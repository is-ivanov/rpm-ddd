import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import HomePage from '../components/HomePage.vue';

describe('HomePage', () => {
  it('renders the application title', () => {
    const wrapper = mount(HomePage);

    expect(wrapper.get('[data-testid="home-title"]').text()).toBe('RPM');
  });
});
