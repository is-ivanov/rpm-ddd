<script setup lang="ts">
import { computed, ref } from 'vue';
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

const isFormValid = computed(() => isLoginFormValid(loginName.value, password.value));

async function submitLogin(): Promise<void> {
  try {
    await login({ login: loginName.value, password: password.value });
  } catch (error) {
    showLoginError(error);
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
          />
          <p v-if="fieldErrors.password" data-testid="password-error" class="field-error">{{ fieldErrors.password }}</p>
        </div>

        <button type="submit" data-testid="submit-button" class="btn-primary mt-2" :disabled="!isFormValid">
          Sign In
        </button>
      </form>
    </div>
  </main>
</template>
