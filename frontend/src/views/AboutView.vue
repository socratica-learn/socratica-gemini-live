<script setup lang="ts">
//
</script>

<template>
  <div class="about-view">
    <div class="container mx-auto px-4 pt-0 pb-16">
      <h1 class="text-4xl font-bold text-gray-900 mb-8">About Socratica</h1>
      
      <div class="prose prose-lg max-w-3xl">
        <p class="text-gray-700 mb-4">
          Socratica is an AI-powered interactive study assistant designed to help students 
          master their subjects through active learning and Socratic questioning.
        </p>

        <h2 class="text-2xl font-semibold text-gray-900 mt-8 mb-4">Our Mission</h2>
        <p class="text-gray-700 mb-4">
          We believe that true learning comes from explanation and active engagement. 
          Socratica transforms passive studying into an interactive experience by 
          simulating a knowledgeable tutor who asks the right questions at the right time.
        </p>

        <h2 class="text-2xl font-semibold text-gray-900 mt-8 mb-4">Key Features</h2>
        <ul class="list-disc list-inside text-gray-700 space-y-2">
          <li>Interactive voice-based study sessions</li>
          <li>AI-powered Socratic questioning</li>
          <li>Automated note summarization</li>
          <li>Quiz generation from study materials</li>
          <li>Progress tracking and analytics</li>
          <li>Study planning and scheduling</li>
        </ul>

        <h2 class="text-2xl font-semibold text-gray-900 mt-8 mb-4">Technology Stack</h2>
        <div class="grid md:grid-cols-2 gap-6 mt-4">
          <div class="bg-gray-50 p-4 rounded-lg">
            <h3 class="font-semibold text-gray-900 mb-2">Backend</h3>
            <ul class="text-gray-700 text-sm space-y-1">
              <li>Java 17 + Spring Boot</li>
              <li>PostgreSQL Database</li>
              <li>OpenAI API Integration</li>
              <li>JWT Authentication</li>
            </ul>
          </div>
          <div class="bg-gray-50 p-4 rounded-lg">
            <h3 class="font-semibold text-gray-900 mb-2">Frontend</h3>
            <ul class="text-gray-700 text-sm space-y-1">
              <li>Vue 3 + TypeScript</li>
              <li>Vite Build Tool</li>
              <li>TailwindCSS</li>
              <li>Pinia State Management</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-view {
  min-height: 100vh;
}
</style>

