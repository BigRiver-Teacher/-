<template>
  <div class="expert-consult">
    <ChatPanel type="CONSULT" />
  </div>
</template>

<script setup lang="ts">
import ChatPanel from '@/components/ChatPanel.vue'
</script>

<style scoped>
.expert-consult {
  height: calc(100vh - 160px);
  min-height: 480px;
}
</style>
