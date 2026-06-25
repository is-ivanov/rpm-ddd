import { describe, expect, it } from 'vitest';
import { http, HttpResponse, type JsonBodyType } from 'msw';
import { server } from '@/test/msw-server';
import { getAppInfo } from '../logic/app-version.api';
import type { AppInfo } from '../logic/app-version.types';

const BASE = import.meta.env.VITE_API_URL;

const INFO_PATH = '/actuator/info';

const COMMIT_ID = '1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b';

function stubInfo(body: JsonBodyType): void {
  server.use(http.get(`${BASE}${INFO_PATH}`, () => HttpResponse.json(body, { status: 200 })));
}

describe('App Info API Client', () => {
  it('maps GET /actuator/info into the AppInfo subset', async () => {
    stubInfo({
      git: {
        branch: 'main',
        commit: { id: COMMIT_ID, time: '2026-06-26T10:15:30Z' },
      },
      build: {
        artifact: 'rpm-ddd',
        name: 'rpm-ddd',
        time: '2026-06-26T10:20:00Z',
        version: '0.0.1-SNAPSHOT',
        group: 'by.iivanov.rpm',
      },
    });

    const result = await getAppInfo();

    const expected: AppInfo = {
      build: { version: '0.0.1-SNAPSHOT', time: '2026-06-26T10:20:00Z' },
      git: { commit: { id: COMMIT_ID } },
    };
    expect(result).toEqual(expected);
  });
});
