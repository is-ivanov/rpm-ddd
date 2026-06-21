<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Check } from '@lucide/vue';
import { activateAccount, validateActivationToken } from '../logic/activation.api';
import { mapActivationSubmitErrorToView } from '../logic/activation-error-view.logic';
import { evaluateComplexityRules } from '../logic/password-strength.logic';
import { evaluatePasswordMatch } from '../logic/password-match.logic';
import { ActivationError, type ActivationTokenResponse } from '../logic/types';
import PasswordField from './PasswordField.vue';
import AppLogo from '@/app/components/AppLogo.vue';
import LoadingButton from '@/app/components/LoadingButton.vue';
import ActivationSuccess from './ActivationSuccess.vue';
import ActivationExpired from './ActivationExpired.vue';
import ActivationErrorBanner from './ActivationErrorBanner.vue';

const route = useRoute();
const account = ref<ActivationTokenResponse | null>(null);
const password = ref('');
const complexityRules = computed(() => evaluateComplexityRules(password.value));
const confirmPassword = ref('');
const passwordMatch = computed(() => evaluatePasswordMatch(password.value, confirmPassword.value));
const showMismatchError = computed(() => confirmPassword.value.length > 0 && !passwordMatch.value.matched);
const activated = ref(false);
const tokenInvalid = ref(false);
const submitError = ref('');
const submitting = ref(false);

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
  submitError.value = '';
  submitting.value = true;
  try {
    await activateAccount(tokenFromRoute(), password.value);
    activated.value = true;
  } catch (error) {
    submitError.value = mapActivationSubmitErrorToView(error).errorMessage;
  } finally {
    submitting.value = false;
  }
}

function tokenFromRoute(): string {
  const token = route.query.token;
  return typeof token === 'string' ? token : '';
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-surface font-sans">
    <ActivationSuccess v-if="activated" />
    <ActivationExpired v-else-if="tokenInvalid" />
    <div v-else class="auth-card">
      <AppLogo class="mb-6 text-center" />
      <div class="mb-1 text-lg font-semibold text-ink">Set Password</div>
      <div v-if="account" class="mb-5 text-sm text-muted">For account {{ account.login }} ({{ account.email }})</div>

      <ActivationErrorBanner v-if="submitError" :message="submitError" />

      <form @submit.prevent="submitActivation">
        <div class="mb-4">
          <label for="activation-password" class="mb-1.5 block text-sm font-medium text-ink">New password</label>
          <PasswordField
            v-model="password"
            input-id="activation-password"
            name="password"
            input-test-id="activation-password-input"
            toggle-test-id="activation-password-toggle"
            :disabled="submitting"
          />
        </div>

        <div data-testid="password-complexity-rules" class="mt-2 mb-4">
          <div
            v-for="rule in complexityRules"
            :key="rule.key"
            :data-testid="`complexity-rule-${rule.key}`"
            :data-met="rule.met"
            class="complexity-rule"
          >
            <Check class="shrink-0" :size="16" />
            {{ rule.label }}
          </div>
        </div>

        <div class="mb-4">
          <label for="activation-confirm" class="mb-1.5 block text-sm font-medium text-ink">Confirm password</label>
          <PasswordField
            v-model="confirmPassword"
            input-id="activation-confirm"
            name="confirmPassword"
            input-test-id="activation-confirm-password-input"
            toggle-test-id="activation-confirm-password-toggle"
            placeholder="Re-enter password"
            :disabled="submitting"
          />
          <p v-if="showMismatchError" data-testid="password-mismatch-error" class="field-error">
            {{ passwordMatch.error }}
          </p>
        </div>

        <LoadingButton
          class="mt-5"
          test-id="activate-button"
          loading-test-id="activate-loading"
          label="Activate Account"
          loading-label="Activating…"
          :loading="submitting"
        />
      </form>
    </div>
  </main>
</template>
