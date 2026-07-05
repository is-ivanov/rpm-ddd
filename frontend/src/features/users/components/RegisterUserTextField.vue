<script setup lang="ts">
defineProps<{
  fieldId: string;
  label: string;
  placeholder: string;
  optional?: boolean;
  disabled?: boolean;
  error?: string;
}>();

const model = defineModel<string>({ required: true });
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :data-testid="`${fieldId}-label`" :for="fieldId" class="text-sm font-medium">
      {{ label }} <span v-if="optional" class="font-normal text-muted">— optional</span>
    </label>
    <input
      :id="fieldId"
      v-model="model"
      :data-testid="fieldId"
      class="form-input"
      :class="{ 'border-danger': error }"
      :placeholder="placeholder"
      :disabled="disabled"
    />
    <p v-if="error" :data-testid="`${fieldId}-error`" class="field-error">{{ error }}</p>
  </div>
</template>
