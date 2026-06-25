import type { AppInfo, AppVersion } from './app-version.types';

export function buildAppVersion(info: AppInfo): AppVersion {
  return {
    version: info.build.version,
    commit: info.git.commit.id.slice(0, 7),
    buildTime: info.build.time,
  };
}
