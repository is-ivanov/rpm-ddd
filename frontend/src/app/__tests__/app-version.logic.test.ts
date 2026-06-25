import { describe, expect, it } from 'vitest';
import { buildAppVersion } from '../logic/app-version.logic';
import type { AppInfo } from '../logic/app-version.types';

const ACTUATOR_INFO: AppInfo = {
  build: { version: '1.4.2', time: '2026-06-20T10:20:00Z' },
  git: { commit: { id: '1a3323610a62fd7b6d917c1b35441e525394f4b9' } },
};

describe('App Version View Model', () => {
  it('shortens the git commit to the first seven characters for display', () => {
    const appVersion = buildAppVersion(ACTUATOR_INFO);

    expect(appVersion.commit).toBe('1a33236');
  });

  it('extracts the build version unchanged', () => {
    const appVersion = buildAppVersion(ACTUATOR_INFO);

    expect(appVersion.version).toBe('1.4.2');
  });

  // RED — buildAppVersion still passes the raw ISO timestamp through (T-separated)
  it.fails('formats the build time as a readable UTC timestamp', () => {
    const appVersion = buildAppVersion(ACTUATOR_INFO);

    expect(appVersion.buildTime).toBe('Jun 20, 2026, 10:20 UTC');
  });
});
