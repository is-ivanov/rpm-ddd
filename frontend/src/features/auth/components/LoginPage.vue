<script setup lang="ts">
import { ref } from 'vue';
import { Eye, EyeOff } from '@lucide/vue';

const showPassword = ref(false);
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-[#f8f9fa] font-sans">
    <div class="w-full max-w-[400px] rounded-lg bg-white p-6 shadow-[0_1px_3px_rgba(0,0,0,0.08)]">
      <div class="mb-6 text-center text-2xl font-bold text-[#228be6]">RPM</div>
      <div class="mb-6 text-center text-lg font-semibold text-[#212529]">Sign In</div>

      <form>
        <div class="mb-4">
          <label for="login" class="mb-1.5 block text-sm font-medium text-[#212529]">Username</label>
          <input
            id="login"
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
              name="password"
              :type="showPassword ? 'text' : 'password'"
              data-testid="password-input"
              placeholder="Enter password"
              class="form-input pr-[38px]"
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

        <button
          type="submit"
          data-testid="submit-button"
          class="mt-2 h-10 w-full rounded-md bg-[#228be6] text-sm font-medium text-white transition-colors hover:bg-[#1c7ed6]"
        >
          Sign In
        </button>
      </form>

      <a class="mt-4 block cursor-default text-center text-[13px] text-[#adb5bd] no-underline" aria-disabled="true">
        Forgot password?
      </a>
    </div>
  </main>
</template>
