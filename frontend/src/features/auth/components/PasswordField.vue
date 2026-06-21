<script setup lang="ts">
import { ref } from 'vue';
import { Eye, EyeOff } from '@lucide/vue';

defineProps<{
  inputId: string;
  name: string;
  inputTestId: string;
  toggleTestId: string;
  placeholder?: string;
  disabled?: boolean;
}>();

const model = defineModel<string>({ required: true });
const showPassword = ref(false);
</script>

<template>
  <div class="relative">
    <input
      :id="inputId"
      v-model="model"
      :name="name"
      :type="showPassword ? 'text' : 'password'"
      :data-testid="inputTestId"
      :placeholder="placeholder"
      :disabled="disabled"
      class="form-input pr-10"
    />
    <button
      type="button"
      :data-testid="toggleTestId"
      :aria-label="showPassword ? 'Hide password' : 'Show password'"
      :disabled="disabled"
      class="icon-button absolute right-2 top-1/2 -translate-y-1/2 p-1 text-muted hover:text-ink disabled:cursor-not-allowed disabled:opacity-60"
      @click="showPassword = !showPassword"
    >
      <EyeOff v-if="showPassword" :size="16" />
      <Eye v-else :size="16" />
    </button>
  </div>
</template>
