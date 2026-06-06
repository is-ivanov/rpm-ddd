<script setup lang="ts">
import { ref } from 'vue';
import { Eye, EyeOff } from '@lucide/vue';
import { login } from '../logic/login.api';
import { LoginError } from '../logic/types';
import LoginErrorBanner from './LoginErrorBanner.vue';

const loginName = ref('');
const password = ref('');
const showPassword = ref(false);
const errorMessage = ref('');
const requiresActivation = ref(false);

async function submitLogin(): Promise<void> {
  try {
    await login({ login: loginName.value, password: password.value });
  } catch (error) {
    if (error instanceof LoginError) {
      showLoginError(error);
    }
  }
}

function showLoginError(error: LoginError): void {
  errorMessage.value = error.message;
  requiresActivation.value = error.requiresActivation;
  loginName.value = '';
  password.value = '';
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-[#f8f9fa] font-sans">
    <div class="auth-card">
      <div class="mb-6 text-center text-2xl font-bold text-[#228be6]">RPM</div>
      <div class="mb-6 text-center text-lg font-semibold text-[#212529]">Sign In</div>

      <LoginErrorBanner v-if="errorMessage" :message="errorMessage" :requires-activation="requiresActivation" />

      <form @submit.prevent="submitLogin">
        <div class="mb-4">
          <label for="login" class="mb-1.5 block text-sm font-medium text-[#212529]">Username</label>
          <input
            id="login"
            v-model="loginName"
            name="login"
            type="text"
            data-testid="login-input"
            placeholder="Enter username"
            class="form-input"
          />
        </div>

        <div class="mb-4">
          <label for="password" class="mb-1.5 block text-sm font-medium text-[#212529]">Password</label>
          <div class="relative">
            <input
              id="password"
              v-model="password"
              name="password"
              :type="showPassword ? 'text' : 'password'"
              data-testid="password-input"
              placeholder="Enter password"
              class="form-input pr-9.5"
            />
            <button
              type="button"
              data-testid="password-toggle"
              :aria-label="showPassword ? 'Hide password' : 'Show password'"
              class="absolute right-2.5 top-1/2 flex -translate-y-1/2 cursor-pointer items-center border-none bg-transparent p-0 text-[#6c757d] hover:text-[#212529]"
              @click="showPassword = !showPassword"
            >
              <EyeOff v-if="showPassword" :size="18" />
              <Eye v-else :size="18" />
            </button>
          </div>
        </div>

        <button type="submit" data-testid="submit-button" class="btn-primary mt-2">Sign In</button>
      </form>

      <a class="mt-4 block cursor-default text-center text-[13px] text-[#adb5bd] no-underline" aria-disabled="true">
        Forgot password?
      </a>
    </div>
  </main>
</template>
