<script setup lang="ts">
import { ChevronDown, X } from '@lucide/vue';
import RegisterUserTextField from './RegisterUserTextField.vue';

const APP_DEFAULT_TIMEZONE_LABEL = '(UTC+01:00) Central European Time — Europe/Berlin';

type TextFieldConfig = { fieldId: string; label: string; placeholder: string; optional?: boolean };

const TEXT_FIELDS: readonly TextFieldConfig[] = [
  { fieldId: 'register-user-first-name', label: 'First name', placeholder: 'e.g. Sarah' },
  { fieldId: 'register-user-middle-name', label: 'Middle name', placeholder: 'e.g. Jane', optional: true },
  { fieldId: 'register-user-last-name', label: 'Last name', placeholder: 'e.g. Connor' },
  { fieldId: 'register-user-login', label: 'Login', placeholder: 's.connor' },
  { fieldId: 'register-user-email', label: 'Email', placeholder: 's.connor@rpm.local' },
];

defineEmits<{ close: [] }>();
</script>

<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div data-testid="register-user-modal" class="modal-card">
      <div class="flex items-center justify-between px-6 pt-5">
        <div class="text-lg font-semibold">Register user</div>
        <button
          data-testid="register-user-close"
          type="button"
          class="icon-button h-8 w-8 justify-center rounded text-muted"
          aria-label="Close"
          @click="$emit('close')"
        >
          <X :size="20" aria-hidden="true" />
        </button>
      </div>

      <div class="flex flex-col gap-4 px-6 pb-6 pt-4">
        <RegisterUserTextField
          v-for="field in TEXT_FIELDS"
          :key="field.fieldId"
          :field-id="field.fieldId"
          :label="field.label"
          :placeholder="field.placeholder"
          :optional="field.optional"
        />

        <div class="flex flex-col gap-1.5">
          <label data-testid="register-user-timezone-label" class="text-sm font-medium"> Timezone </label>
          <div data-testid="register-user-timezone" class="select-control">
            <span>{{ APP_DEFAULT_TIMEZONE_LABEL }}</span>
            <ChevronDown :size="16" class="shrink-0 text-muted" aria-hidden="true" />
          </div>
          <span class="text-xs text-muted"
            >Used to display dates for this user. Defaults to the application timezone.</span
          >
        </div>
      </div>

      <div class="flex justify-end gap-3 px-6 pb-6">
        <button
          data-testid="register-user-cancel"
          type="button"
          class="h-10 cursor-pointer rounded-md border border-line px-4 text-sm font-medium text-ink"
          @click="$emit('close')"
        >
          Cancel
        </button>
        <button
          data-testid="register-user-submit"
          type="button"
          class="inline-flex h-10 cursor-pointer items-center rounded-md bg-accent px-4 text-sm font-medium text-white hover:bg-accent-hover"
        >
          Register
        </button>
      </div>
    </div>
  </div>
</template>
