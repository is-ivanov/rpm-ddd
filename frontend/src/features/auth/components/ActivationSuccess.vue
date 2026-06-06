<script setup lang="ts">
import { useRouter } from 'vue-router';
import { CheckCircle2 } from '@lucide/vue';

const router = useRouter();

function goToSignIn(): void {
  router.push('/login');
}
</script>

<template>
  <div data-testid="activation-success" class="auth-card text-center">
    <div class="mb-6 text-2xl font-bold text-[#228be6]">RPM</div>
    <div data-testid="activation-success-icon" class="mb-4 flex justify-center text-[#40c057]">
      <CheckCircle2 :size="48" />
    </div>
    <div data-testid="activation-success-title" class="mb-2 text-lg font-semibold text-[#212529]">
      Account Activated!
    </div>
    <p class="mb-6 text-sm leading-[1.5] text-[#6c757d]">Your password has been set. You can now sign in.</p>
    <button
      type="button"
      data-testid="go-to-sign-in-button"
      class="btn-primary block cursor-pointer"
      @click="goToSignIn"
    >
      Go to Sign In
    </button>
  </div>
</template>
