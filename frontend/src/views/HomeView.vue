<script setup lang="ts">
//
</script>

<template>
  <div class="home-page">
    <!-- Empty for now -->
  </div>
</template>

<style scoped>
.home-page {
  width: 100%;
  height: 100vh;
  background: #000000;
  position: absolute;
  top: 0;
  left: 0;
  margin: 0;
  padding: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Global typography - Inter family for all text */
.home-page,
.home-page * {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Helvetica Neue', sans-serif;
}

/* Title and subtitle: uppercase/condensed styling on top of Inter */
.home-page h1,
.home-page h2,
.home-page .title,
.home-page .subtitle {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Helvetica Neue', sans-serif;
  text-transform: uppercase;
  font-stretch: condensed;
  letter-spacing: 0.05em;
}

/* Buttons/nav copy: Inter with custom tracking and weights */
.home-page button,
.home-page .btn,
.home-page nav,
.home-page .nav-link {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu,
    Cantarell, 'Helvetica Neue', sans-serif;
  letter-spacing: 0.02em;
  font-weight: 500;
}
</style>
