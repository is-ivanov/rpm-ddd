import type { AppInfo, AppVersion } from './app-version.types';

const BUILD_TIME_FORMAT = new Intl.DateTimeFormat('en-US', {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
  timeZone: 'UTC',
  timeZoneName: 'short',
});

export function buildAppVersion(info: AppInfo): AppVersion {
  return {
    version: info.build.version,
    commit: info.git.commit.id.slice(0, 7),
    buildTime: formatBuildTime(info.build.time),
  };
}

function formatBuildTime(isoTime: string): string {
  return BUILD_TIME_FORMAT.format(new Date(isoTime));
}
