import { afterAll, afterEach, beforeAll, beforeEach } from 'vitest';
import { installInMemoryLocalStorage } from './local-storage';
import { server } from './msw-server';

const localStorageStub = installInMemoryLocalStorage();

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
beforeEach(() => localStorageStub.clear());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
