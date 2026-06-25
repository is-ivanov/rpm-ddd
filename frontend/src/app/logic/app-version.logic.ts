import type { AppInfo, AppVersion } from './app-version.types';

export function buildAppVersion(info: AppInfo): AppVersion {
  void info;
  return { version: '', commit: '', buildTime: '' };
}
