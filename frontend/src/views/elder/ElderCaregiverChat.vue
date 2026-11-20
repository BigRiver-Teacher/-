<script setup lang="ts">
import ChatPanel from '@/components/ChatPanel.vue'
</script>

<template>
  <div class="elder-caregiver-chat">
    <ChatPanel type="CAREGIVER" />
  </div>
</template>

<style scoped>
.elder-caregiver-chat {
  height: calc(100vh - 150px);
  min-height: 480px;
}
</style>
