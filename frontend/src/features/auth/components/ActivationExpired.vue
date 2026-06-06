<script setup lang="ts">
import { useRouter } from 'vue-router';
import { XCircle } from '@lucide/vue';
import ActivationResultCard from './ActivationResultCard.vue';

const router = useRouter();

function requestNewLink(): void {
  router.push('/login');
}
</script>

<template>
  <ActivationResultCard
    test-id="activation-error"
    :icon="XCircle"
    icon-color-class="text-[#fa5252]"
    title="Link Expired"
    description="The activation link is invalid or has expired. Please request a new one."
    button-test-id="request-new-link-button"
    button-label="Request New Link"
    @action="requestNewLink"
  />
</template>
