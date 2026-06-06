<script setup lang="ts">
import { ref } from 'vue';
import { Eye, EyeOff } from '@lucide/vue';

defineProps<{
  inputId: string;
  name: string;
  inputTestId: string;
  toggleTestId: string;
  placeholder?: string;
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
      class="form-input pr-10"
    />
    <button
      type="button"
      :data-testid="toggleTestId"
      :aria-label="showPassword ? 'Hide password' : 'Show password'"
      class="absolute right-2 top-1/2 flex -translate-y-1/2 cursor-pointer items-center border-none bg-transparent p-1 text-[#6c757d] hover:text-[#212529]"
      @click="showPassword = !showPassword"
    >
      <EyeOff v-if="showPassword" :size="16" />
      <Eye v-else :size="16" />
    </button>
  </div>
</template>
