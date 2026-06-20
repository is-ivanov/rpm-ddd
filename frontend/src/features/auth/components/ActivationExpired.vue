<script setup lang="ts">
import { useRouter } from 'vue-router';
import { XCircle } from '@lucide/vue';
import ActivationResultCard from './ActivationResultCard.vue';
import { LOGIN_PATH } from '@/app/logic/unauthorized-redirect.logic';

const router = useRouter();

function requestNewLink(): void {
  void router.push(LOGIN_PATH);
}
</script>

<template>
  <ActivationResultCard
    test-id="activation-error"
    :icon="XCircle"
    icon-color-class="text-danger"
    title="Link Expired"
    description="The activation link is invalid or has expired. Please request a new one."
    button-test-id="request-new-link-button"
    button-label="Request New Link"
    @action="requestNewLink"
  />
</template>
