<template>
  <router-view v-slot="{ Component }">
    <transition name="page-fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <AiBubble />
</template>

<script setup>
import AiBubble from './components/AiBubble.vue'
</script>
