<template>
  <router-view v-slot="{ Component }">
    <component :is="Component" :key="$route.fullPath" />
  </router-view>
  <AiBubble />
</template>

<script setup>
import AiBubble from './components/AiBubble.vue'
</script>
