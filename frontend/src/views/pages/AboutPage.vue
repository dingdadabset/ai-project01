<script setup lang="ts">
import { onMounted } from 'vue'
import gsap from 'gsap'

onMounted(() => {
  const tl = gsap.timeline()
  
  tl.fromTo('.about-hero h1',
    { opacity: 0, y: 50 },
    { opacity: 1, y: 0, duration: 0.8, ease: 'power3.out' }
  )
  .fromTo('.about-hero p',
    { opacity: 0, y: 30 },
    { opacity: 1, y: 0, duration: 0.6, ease: 'power3.out' },
    '-=0.4'
  )
  .fromTo('.about-content',
    { opacity: 0, y: 30 },
    { opacity: 1, y: 0, duration: 0.6, ease: 'power3.out' },
    '-=0.3'
  )
})
</script>

<template>
  <div class="about-page">
    <!-- Hero Section -->
    <section class="about-hero">
      <div class="container">
        <h1>About This Blog</h1>
        <p class="hero-desc">
          A modern blog platform built with Vue 3, TypeScript, and Spring Boot
        </p>
      </div>
    </section>
    
    <!-- Content -->
    <section class="about-content section">
      <div class="container content-container">
        <div class="content-block">
          <h2>✨ Our Mission</h2>
          <p>
            We believe in sharing knowledge and creating beautiful reading experiences.
            This blog platform is designed to provide an immersive, interactive way
            to discover and enjoy content.
          </p>
        </div>
        
        <div class="content-block">
          <h2>🛠️ Technology Stack</h2>
          <div class="tech-grid">
            <div class="tech-item">
              <span class="tech-icon">⚡</span>
              <h3>Vue 3</h3>
              <p>Modern reactive framework</p>
            </div>
            <div class="tech-item">
              <span class="tech-icon">📘</span>
              <h3>TypeScript</h3>
              <p>Type-safe development</p>
            </div>
            <div class="tech-item">
              <span class="tech-icon">🎨</span>
              <h3>GSAP</h3>
              <p>Smooth animations</p>
            </div>
            <div class="tech-item">
              <span class="tech-icon">🚀</span>
              <h3>Spring Boot</h3>
              <p>Powerful backend</p>
            </div>
            <div class="tech-item">
              <span class="tech-icon">🗄️</span>
              <h3>MyBatis Plus</h3>
              <p>Efficient ORM</p>
            </div>
            <div class="tech-item">
              <span class="tech-icon">💾</span>
              <h3>MySQL</h3>
              <p>Reliable database</p>
            </div>
          </div>
        </div>
        
        <div class="content-block">
          <h2>🎯 Features</h2>
          <ul class="feature-list">
            <li>📝 Full CRUD for posts, categories, tags, comments</li>
            <li>🎬 Story mode with immersive animations</li>
            <li>📋 List mode for quick browsing</li>
            <li>🔍 Search and filter functionality</li>
            <li>💬 Comment system with guest support</li>
            <li>👤 User management and roles</li>
            <li>📱 Fully responsive design</li>
            <li>✨ Smooth page transitions</li>
          </ul>
        </div>
        
        <div class="content-block">
          <h2>📬 Contact</h2>
          <p>
            Have questions or feedback? We'd love to hear from you!
          </p>
          <div class="contact-links">
            <a href="https://github.com" class="btn btn-secondary">
              GitHub
            </a>
            <a href="mailto:hello@blog.com" class="btn btn-primary">
              Email Us
            </a>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
}

.about-hero {
  padding: 120px 0 80px;
  text-align: center;
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.2) 0%, transparent 100%);
}

.about-hero h1 {
  font-size: clamp(2.5rem, 5vw, 4rem);
  margin-bottom: 24px;
}

.hero-desc {
  font-size: 1.25rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

.content-container {
  max-width: 800px;
}

.content-block {
  margin-bottom: 64px;
}

.content-block h2 {
  font-size: 1.75rem;
  margin-bottom: 24px;
}

.content-block p {
  color: var(--text-secondary);
  font-size: 1.1rem;
  line-height: 1.8;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
  margin-top: 32px;
}

.tech-item {
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  text-align: center;
  transition: all 0.3s ease;
}

.tech-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 30px rgba(99, 102, 241, 0.2);
}

.tech-icon {
  font-size: 2.5rem;
  display: block;
  margin-bottom: 16px;
}

.tech-item h3 {
  font-size: 1.1rem;
  margin-bottom: 8px;
}

.tech-item p {
  font-size: 0.875rem;
  margin: 0;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  padding: 12px 0;
  color: var(--text-secondary);
  font-size: 1.1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.contact-links {
  display: flex;
  gap: 16px;
  margin-top: 24px;
}
</style>
