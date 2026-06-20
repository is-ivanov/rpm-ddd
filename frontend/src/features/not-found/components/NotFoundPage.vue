<template>
  <main data-testid="not-found-page" class="flex min-h-screen flex-col items-center justify-center gap-4 bg-surface">
    <h1 data-testid="not-found-title" class="text-3xl font-semibold text-ink">404</h1>
    <p data-testid="not-found-message" class="text-muted">Page not found</p>
    <RouterLink data-testid="back-home-link" to="/" class="link-primary"> Back to home </RouterLink>
  </main>
</template>
