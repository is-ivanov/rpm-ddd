import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';

const clientApiBaseUrl = process.env.VITE_API_URL || '';
const devProxyTarget = process.env.VITE_API_URL || 'http://localhost:8080';
const frontendPort = Number(process.env.FRONTEND_PORT) || 5173;

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  define: {
    'import.meta.env.VITE_API_URL': JSON.stringify(clientApiBaseUrl),
  },
  server: {
    port: frontendPort,
    proxy: {
      '/api': {
        target: devProxyTarget,
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['allure-vitest/setup', 'src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    exclude: ['node_modules', 'dist', 'acceptance/**'],
    reporters: [
      'default',
      [
        'allure-vitest/reporter',
        {
          resultsDir: '../target/allure-results',
          links: {
            issue: {
              urlTemplate: 'https://github.com/is-ivanov/rpm-ddd/issues/%s',
              nameTemplate: 'Issue #%s',
            },
          },
        },
      ],
    ],
  },
});
