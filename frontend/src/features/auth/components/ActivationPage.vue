<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Check } from '@lucide/vue';
import { activateAccount, validateActivationToken } from '../logic/activation.api';
import { ActivationError, type ActivationTokenResponse } from '../logic/types';
import PasswordField from './PasswordField.vue';
import ActivationSuccess from './ActivationSuccess.vue';
import ActivationExpired from './ActivationExpired.vue';

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
const activated = ref(false);
const tokenInvalid = ref(false);

onMounted(loadAccount);

async function loadAccount(): Promise<void> {
  try {
    account.value = await validateActivationToken(tokenFromRoute());
  } catch (error) {
    if (error instanceof ActivationError) {
      tokenInvalid.value = true;
    }
  }
}

async function submitActivation(): Promise<void> {
  await activateAccount(tokenFromRoute(), password.value);
  activated.value = true;
}

function tokenFromRoute(): string {
  const token = route.query.token;
  return typeof token === 'string' ? token : '';
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-[#f8f9fa] font-sans">
    <ActivationSuccess v-if="activated" />
    <ActivationExpired v-else-if="tokenInvalid" />
    <div v-else class="auth-card">
      <div class="mb-6 text-center text-2xl font-bold text-[#228be6]">RPM</div>
      <div class="mb-1 text-lg font-semibold text-[#212529]">Set Password</div>
      <div v-if="account" class="mb-5 text-sm text-[#6c757d]">
        For account {{ account.login }} ({{ account.email }})
      </div>

      <form @submit.prevent="submitActivation">
        <div class="mb-4">
          <label for="activation-password" class="mb-1.5 block text-sm font-medium text-[#212529]">New password</label>
          <PasswordField
            v-model="password"
            input-id="activation-password"
            name="password"
            input-test-id="activation-password-input"
            toggle-test-id="activation-password-toggle"
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
            input-test-id="activation-confirm-password-input"
            toggle-test-id="activation-confirm-password-toggle"
            placeholder="Re-enter password"
          />
        </div>

        <button type="submit" data-testid="activate-button" class="btn-primary mt-5">Activate Account</button>
      </form>
    </div>
  </main>
</template>
