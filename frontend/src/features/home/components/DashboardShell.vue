<script setup lang="ts">
import { LayoutDashboard } from '@lucide/vue';
import DashboardTopBar from './DashboardTopBar.vue';
</script>

<template>
  <div data-testid="dashboard-shell" class="flex min-h-screen flex-col bg-surface font-sans">
    <DashboardTopBar />
    <div class="flex min-h-0 flex-1">
      <aside data-testid="dashboard-sidebar" class="flex w-60 shrink-0 items-center justify-center bg-sidebar p-6">
        <div class="text-center text-[13px] leading-normal text-sidebar-muted opacity-50">
          Sections will appear<br />as modules are connected
        </div>
      </aside>
      <main class="flex flex-1 flex-col p-8">
        <h1 data-testid="page-title" class="text-2xl font-bold text-ink">Home</h1>
        <div
          data-testid="dashboard-placeholder"
          class="flex flex-1 flex-col items-center justify-center gap-3 text-center"
        >
          <LayoutDashboard :size="48" class="text-placeholder" aria-hidden="true" />
          <div class="text-lg font-semibold text-ink">Dashboard</div>
          <p class="max-w-90 text-sm leading-normal text-muted">
            Patient, order, and alert data will appear here as modules are connected.
          </p>
        </div>
      </main>
    </div>
  </div>
</template>
