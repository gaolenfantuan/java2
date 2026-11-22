<script setup>
import ComicPanel from './components/ComicPanel.vue'
import LoginPanel from './components/LoginPanel.vue'
</script>

<template>
  <div class="bg-deco" aria-hidden="true">
    <div class="burst one"></div>
    <div class="burst two"></div>
    <div class="float a">🐾</div>
    <div class="float b">💛</div>
    <div class="float c">🦴</div>
    <div class="float d">🐾</div>
  </div>

  <main class="stage">
    <ComicPanel />
    <LoginPanel />
  </main>
</template>
