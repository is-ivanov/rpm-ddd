<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Check } from '@lucide/vue';
import { validateActivationToken } from '../logic/activation.api';
import type { ActivationTokenResponse } from '../logic/types';
import PasswordField from './PasswordField.vue';

const PASSWORD_RULES = [
  'At least 12 characters',
  'At least one uppercase letter',
  'At least one lowercase letter',
  'At least one digit',
  'At least one special character',
  'No spaces',
] as const;

const route = useRoute();
const account = ref<ActivationTokenResponse | null>(null);
const password = ref('');
const confirmPassword = ref('');

onMounted(loadAccount);

async function loadAccount(): Promise<void> {
  try {
    account.value = await validateActivationToken(tokenFromRoute());
  } catch {
    account.value = null;
  }
}

function tokenFromRoute(): string {
  const token = route.query.token;
  return typeof token === 'string' ? token : '';
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-[#f8f9fa] font-sans">
    <div class="w-full max-w-100 rounded-lg bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.08)]">
      <div class="mb-6 text-center text-2xl font-bold text-[#228be6]">RPM</div>
      <div class="mb-1 text-lg font-semibold text-[#212529]">Set Password</div>
      <div v-if="account" class="mb-5 text-sm text-[#6c757d]">
        For account {{ account.login }} ({{ account.email }})
      </div>

      <form>
        <div class="mb-4">
          <label for="activation-password" class="mb-1.5 block text-sm font-medium text-[#212529]">New password</label>
          <PasswordField
            v-model="password"
            input-id="activation-password"
            name="password"
            input-testid="activation-password-input"
            toggle-testid="activation-password-toggle"
          />
        </div>

        <div data-testid="password-complexity-rules" class="mt-2 mb-4">
          <div
            v-for="rule in PASSWORD_RULES"
            :key="rule"
            data-testid="password-complexity-rule"
            class="mb-1 flex items-center gap-2 text-[13px] text-[#6c757d]"
          >
            <Check class="shrink-0" :size="16" />
            {{ rule }}
          </div>
        </div>

        <div class="mb-4">
          <label for="activation-confirm" class="mb-1.5 block text-sm font-medium text-[#212529]"
            >Confirm password</label
          >
          <PasswordField
            v-model="confirmPassword"
            input-id="activation-confirm"
            name="confirmPassword"
            input-testid="activation-confirm-password-input"
            toggle-testid="activation-confirm-password-toggle"
            placeholder="Re-enter password"
          />
        </div>

        <button
          type="submit"
          data-testid="activate-button"
          class="mt-5 h-10 w-full rounded-md bg-[#228be6] text-sm font-medium text-white transition-colors hover:bg-[#1c7ed6]"
        >
          Activate Account
        </button>
      </form>
    </div>
  </main>
</template>
