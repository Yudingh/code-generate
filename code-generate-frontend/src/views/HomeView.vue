<script setup lang="ts">
import TheWelcome from '../components/TheWelcome.vue'
</script>

<template>
  <main class="home-view">
    <TheWelcome />
  </main>
</template>

<style scoped>
.home-view {
  padding: 16px 0 24px;
}

@media (min-width: 1024px) {
  .home-view {
    padding: 28px 0 40px;
  }
}
</style>
