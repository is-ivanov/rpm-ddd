<script setup lang="ts">
import { computed, ref } from 'vue';
import { LoaderCircle } from '@lucide/vue';
import { login } from '../logic/login.api';
import { mapLoginErrorToView, type LoginFieldErrors } from '../logic/login-error-view.logic';
import { isLoginFormValid } from '../logic/login-form.logic';
import LoginErrorBanner from './LoginErrorBanner.vue';
import PasswordField from './PasswordField.vue';
import AppLogo from '@/app/components/AppLogo.vue';

const loginName = ref('');
const password = ref('');
const errorMessage = ref('');
const requiresActivation = ref(false);
const fieldErrors = ref<LoginFieldErrors>({});
const submitting = ref(false);

const isFormValid = computed(() => isLoginFormValid(loginName.value, password.value));

async function submitLogin(): Promise<void> {
  submitting.value = true;
  try {
    await login({ login: loginName.value, password: password.value });
  } catch (error) {
    showLoginError(error);
  } finally {
    submitting.value = false;
  }
}

function showLoginError(error: unknown): void {
  const view = mapLoginErrorToView(error);
  errorMessage.value = view.errorMessage;
  requiresActivation.value = view.requiresActivation;
  fieldErrors.value = view.fieldErrors;
  loginName.value = '';
  password.value = '';
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-surface font-sans">
    <div class="auth-card">
      <AppLogo class="mb-6 text-center" />
      <div class="mb-6 text-center text-lg font-semibold text-ink">Sign In</div>

      <LoginErrorBanner v-if="errorMessage" :message="errorMessage" :requires-activation="requiresActivation" />

      <form @submit.prevent="submitLogin">
        <div class="mb-4">
          <label for="login" class="mb-1.5 block text-sm font-medium text-ink">Username</label>
          <input
            id="login"
            v-model="loginName"
            name="login"
            type="text"
            data-testid="login-input"
            placeholder="Enter username"
            :disabled="submitting"
            class="form-input"
          />
          <p v-if="fieldErrors.login" data-testid="login-error" class="field-error">{{ fieldErrors.login }}</p>
        </div>

        <div class="mb-4">
          <label for="password" class="mb-1.5 block text-sm font-medium text-ink">Password</label>
          <PasswordField
            v-model="password"
            input-id="password"
            name="password"
            input-test-id="password-input"
            toggle-test-id="password-toggle"
            placeholder="Enter password"
            :disabled="submitting"
          />
          <p v-if="fieldErrors.password" data-testid="password-error" class="field-error">{{ fieldErrors.password }}</p>
        </div>

        <button
          type="submit"
          data-testid="submit-button"
          class="btn-primary mt-2 flex items-center justify-center gap-2"
          :disabled="!isFormValid || submitting"
          :aria-busy="submitting"
        >
          <LoaderCircle
            v-if="submitting"
            data-testid="submit-loading"
            :size="16"
            class="animate-spin"
            aria-hidden="true"
          />
          {{ submitting ? 'Signing In…' : 'Sign In' }}
        </button>
      </form>
    </div>
  </main>
</template>
