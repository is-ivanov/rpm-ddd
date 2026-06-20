<script setup lang="ts">
import { useRouter } from 'vue-router';
import { CheckCircle2 } from '@lucide/vue';
import ActivationResultCard from './ActivationResultCard.vue';
import { LOGIN_PATH } from '@/app/logic/unauthorized-redirect.logic';

const router = useRouter();

function goToSignIn(): void {
  void router.push(LOGIN_PATH);
}
</script>

<template>
  <ActivationResultCard
    test-id="activation-success"
    :icon="CheckCircle2"
    icon-color-class="text-success"
    title="Account Activated!"
    description="Your password has been set. You can now sign in."
    button-test-id="go-to-sign-in-button"
    button-label="Go to Sign In"
    @action="goToSignIn"
  />
</template>
