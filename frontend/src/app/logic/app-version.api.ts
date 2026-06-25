import type { AppInfo } from './app-version.types';
import { appInfoResponseSchema } from '@/app/schemas/app-info.schema';
import { apiUrl } from './fetch.api';

const APP_INFO_PATH = '/actuator/info';

export async function getAppInfo(): Promise<AppInfo> {
  const response = await fetch(apiUrl(APP_INFO_PATH), {
    method: 'GET',
    credentials: 'include',
  });

  return appInfoResponseSchema.parse(await response.json());
}
