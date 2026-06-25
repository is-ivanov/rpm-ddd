<script setup lang="ts">
import { LogIn } from '@lucide/vue';
</script>

<template>
  <main data-testid="home-page" class="flex min-h-screen items-center justify-center bg-surface font-sans">
    <div class="max-w-120 px-6 text-center">
      <div data-testid="welcome-logo" class="text-5xl font-bold tracking-tight text-accent">RPM</div>
      <div data-testid="welcome-tagline" class="mt-2 text-lg font-semibold text-ink">Remote Patient Monitoring</div>
      <p class="mt-4 text-sm leading-relaxed text-muted">
        A platform for continuous monitoring of patient health with automatic alerts on abnormal readings.
      </p>
      <RouterLink
        to="/login"
        data-testid="welcome-login-button"
        class="mt-8 inline-flex h-11 items-center justify-center gap-2 rounded-md bg-accent px-8 text-sm font-medium text-white transition-colors hover:bg-accent-hover"
      >
        <LogIn :size="18" aria-hidden="true" />
        Sign in
      </RouterLink>
    </div>
  </main>
</template>
