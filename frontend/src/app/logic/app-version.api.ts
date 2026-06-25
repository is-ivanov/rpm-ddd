import type { AppInfo } from './app-version.types';

export async function getAppInfo(): Promise<AppInfo> {
  return Promise.resolve({
    build: { version: '', time: '' },
    git: { commit: { id: '' } },
  });
}
