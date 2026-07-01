<script setup lang="ts">
defineProps<{
  fieldId: string;
  label: string;
  placeholder: string;
  optional?: boolean;
}>();
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :data-testid="`${fieldId}-label`" :for="fieldId" class="text-sm font-medium">
      {{ label }} <span v-if="optional" class="font-normal text-muted">— optional</span>
    </label>
    <input :id="fieldId" :data-testid="fieldId" class="form-input" :placeholder="placeholder" />
  </div>
</template>
