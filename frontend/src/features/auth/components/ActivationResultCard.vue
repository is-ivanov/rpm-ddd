<script setup lang="ts">
import type { Component } from 'vue';

defineProps<{
  testId: string;
  icon: Component;
  iconColorClass: string;
  title: string;
  description: string;
  buttonTestId: string;
  buttonLabel: string;
}>();

defineEmits<{
  action: [];
}>();
</script>

<template>
  <div :data-testid="testId" class="auth-card text-center">
    <div class="mb-6 text-2xl font-bold text-[#228be6]">RPM</div>
    <div :data-testid="`${testId}-icon`" class="mb-4 flex justify-center" :class="iconColorClass">
      <component :is="icon" :size="48" />
    </div>
    <div :data-testid="`${testId}-title`" class="mb-2 text-lg font-semibold text-[#212529]">{{ title }}</div>
    <p class="mb-6 text-sm leading-normal text-[#6c757d]">{{ description }}</p>
    <button type="button" :data-testid="buttonTestId" class="btn-primary block cursor-pointer" @click="$emit('action')">
      {{ buttonLabel }}
    </button>
  </div>
</template>
