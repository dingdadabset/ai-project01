<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAppStore } from '@/stores/app'
import PostCard from '@/components/common/PostCard.vue'
import CategoryCard from '@/components/common/CategoryCard.vue'
import gsap from 'gsap'

const store = useAppStore()
const heroRef = ref<HTMLElement | null>(null)
const featuredRef = ref<HTMLElement | null>(null)

// Fetch data on mount
onMounted(async () => {
  await Promise.all([
    store.fetchPosts(0, 10),
    store.fetchCategories(),
    store.fetchTags()
  ])
  
  // Hero animations
  const tl = gsap.timeline()
  
  tl.fromTo('.hero-title', 
    { opacity: 0, y: 100, filter: 'blur(20px)' },
    { opacity: 1, y: 0, filter: 'blur(0px)', duration: 1, ease: 'power3.out' }
  )
  .fromTo('.hero-subtitle',
    { opacity: 0, y: 50 },
    { opacity: 1, y: 0, duration: 0.8, ease: 'power3.out' },
    '-=0.5'
  )
  .fromTo('.hero-cta',
    { opacity: 0, scale: 0.8 },
    { opacity: 1, scale: 1, duration: 0.6, ease: 'back.out(1.7)' },
    '-=0.3'
  )
  .fromTo('.hero-decoration',
    { opacity: 0, scale: 0 },
    { opacity: 1, scale: 1, duration: 1, stagger: 0.1, ease: 'elastic.out(1, 0.5)' },
    '-=0.5'
  )
  
  // Scroll parallax for hero
  window.addEventListener('scroll', handleScroll)
})

const handleScroll = () => {
  const scrollY = window.scrollY
  
  gsap.to('.hero-bg-gradient', {
    y: scrollY * 0.3,
    ease: 'none',
    duration: 0
  })
  
  gsap.to('.hero-decoration', {
    y: scrollY * 0.2,
    rotation: scrollY * 0.02,
    ease: 'none',
    duration: 0
  })
}
</script>

<template>
  <div class="home">
    <!-- Page Transition Overlay -->
    <div class="page-transition-overlay"></div>
    
    <!-- Hero Section -->
    <section ref="heroRef" class="hero">
      <div class="hero-bg">
        <div class="hero-bg-gradient"></div>
        <div class="hero-particles">
          <span v-for="i in 20" :key="i" class="particle"></span>
        </div>
      </div>
      
      <div class="hero-decorations">
        <div class="hero-decoration deco-1">✨</div>
        <div class="hero-decoration deco-2">🚀</div>
        <div class="hero-decoration deco-3">💫</div>
        <div class="hero-decoration deco-4">🎨</div>
      </div>
      
      <div class="hero-content container">
        <h1 class="hero-title">
          Welcome to<br/>
          <span class="highlight">Halo Blog</span>
        </h1>
        <p class="hero-subtitle">
          Explore stories, ideas, and inspirations through an immersive reading experience
        </p>
        <div class="hero-cta">
          <RouterLink to="/posts" class="btn btn-primary btn-lg">
            <span>Explore Stories</span>
            <span class="btn-arrow">→</span>
          </RouterLink>
          <button 
            class="btn btn-secondary btn-lg"
            @click="store.toggleViewMode()"
          >
            {{ store.viewMode === 'story' ? '📋 Switch to List' : '🎬 Switch to Story' }}
          </button>
        </div>
        
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">{{ store.totalPosts }}</span>
            <span class="stat-label">Posts</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ store.categories.length }}</span>
            <span class="stat-label">Categories</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ store.tags.length }}</span>
            <span class="stat-label">Tags</span>
          </div>
        </div>
      </div>
      
      <div class="hero-scroll-indicator">
        <span class="scroll-text">Scroll to explore</span>
        <div class="scroll-arrow">↓</div>
      </div>
    </section>
    
    <!-- Featured Posts Section -->
    <section ref="featuredRef" class="featured-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🌟</span>
            Featured Stories
          </h2>
          <p class="section-desc">Handpicked articles that inspire and inform</p>
        </div>
        
        <!-- Story Mode View -->
        <div v-if="store.viewMode === 'story'" class="story-grid">
          <div 
            v-for="(post, index) in store.featuredPosts" 
            :key="post.id"
            class="story-card"
            :class="`story-card-${index + 1}`"
          >
            <PostCard :post="post" :featured="index === 0" />
          </div>
        </div>
        
        <!-- List Mode View -->
        <div v-else class="list-grid">
          <PostCard 
            v-for="post in store.posts" 
            :key="post.id" 
            :post="post"
          />
        </div>
        
        <div class="section-footer">
          <RouterLink to="/posts" class="btn btn-secondary">
            View All Posts →
          </RouterLink>
        </div>
      </div>
    </section>
    
    <!-- Categories Section -->
    <section class="categories-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">📚</span>
            Browse by Category
          </h2>
          <p class="section-desc">Find content that matches your interests</p>
        </div>
        
        <div class="categories-grid">
          <CategoryCard 
            v-for="category in store.categories"
            :key="category.id"
            :category="category"
          />
        </div>
      </div>
    </section>
    
    <!-- Tags Cloud Section -->
    <section class="tags-section section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">
            <span class="title-icon">🏷️</span>
            Popular Tags
          </h2>
        </div>
        
        <div class="tags-cloud">
          <RouterLink
            v-for="tag in store.tags"
            :key="tag.id"
            :to="`/tags/${tag.slug}`"
            class="tag"
          >
            {{ tag.name }}
            <span class="tag-count">{{ tag.postCount }}</span>
          </RouterLink>
        </div>
      </div>
    </section>
    
    <!-- CTA Section -->
    <section class="cta-section section">
      <div class="container">
        <div class="cta-content glass">
          <h2>Ready to Start Reading?</h2>
          <p>Dive into our collection of thoughtfully crafted articles</p>
          <RouterLink to="/posts" class="btn btn-primary btn-lg">
            Explore Now
          </RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home {
  overflow-x: hidden;
}

/* Hero Section */
.hero {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 120px 0 80px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.hero-bg-gradient {
  position: absolute;
  top: -50%;
  left: 50%;
  transform: translateX(-50%);
  width: 150%;
  height: 150%;
  background: radial-gradient(circle at center, 
    rgba(99, 102, 241, 0.3) 0%, 
    rgba(139, 92, 246, 0.2) 30%, 
    transparent 60%);
}

.hero-particles {
  position: absolute;
  inset: 0;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: var(--color-primary);
  border-radius: 50%;
  opacity: 0.5;
  animation: float 10s infinite ease-in-out;
}

.particle:nth-child(odd) {
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-100px) translateX(50px);
  }
  50% {
    transform: translateY(-50px) translateX(-30px);
  }
  75% {
    transform: translateY(-150px) translateX(20px);
  }
}

.hero-decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-decoration {
  position: absolute;
  font-size: 3rem;
  opacity: 0.6;
}

.deco-1 { top: 15%; left: 10%; }
.deco-2 { top: 25%; right: 15%; }
.deco-3 { bottom: 30%; left: 15%; }
.deco-4 { bottom: 20%; right: 10%; }

.hero-content {
  text-align: center;
  z-index: 1;
}

.hero-title {
  font-size: clamp(3rem, 8vw, 6rem);
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 24px;
}

.hero-title .highlight {
  display: inline-block;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: glow 3s ease-in-out infinite alternate;
}

@keyframes glow {
  from {
    text-shadow: 0 0 20px rgba(99, 102, 241, 0.5);
    filter: brightness(1);
  }
  to {
    text-shadow: 0 0 40px rgba(236, 72, 153, 0.5);
    filter: brightness(1.1);
  }
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto 40px;
}

.hero-cta {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-lg {
  padding: 16px 32px;
  font-size: 1rem;
}

.btn-arrow {
  transition: transform 0.3s ease;
}

.btn:hover .btn-arrow {
  transform: translateX(4px);
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-top: 60px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.hero-scroll-indicator {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  animation: bounce 2s infinite;
}

.scroll-text {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.scroll-arrow {
  font-size: 1.5rem;
  color: var(--color-primary);
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  40% {
    transform: translateX(-50%) translateY(-10px);
  }
  60% {
    transform: translateX(-50%) translateY(-5px);
  }
}

/* Section Styles */
.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.title-icon {
  margin-right: 12px;
}

.section-desc {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.section-footer {
  text-align: center;
  margin-top: 48px;
}

/* Story Grid */
.story-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  grid-template-rows: repeat(2, 1fr);
  gap: 24px;
  min-height: 600px;
}

.story-card-1 {
  grid-row: span 2;
}

/* List Grid */
.list-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

/* Categories Grid */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
}

/* Tags Cloud */
.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.tag-count {
  font-size: 0.65rem;
  opacity: 0.7;
  margin-left: 4px;
}

/* CTA Section */
.cta-section {
  text-align: center;
}

.cta-content {
  padding: 80px 40px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(236, 72, 153, 0.1));
}

.cta-content h2 {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.cta-content p {
  color: var(--text-secondary);
  margin-bottom: 32px;
}

@media (max-width: 768px) {
  .hero-stats {
    gap: 32px;
  }
  
  .stat-number {
    font-size: 2rem;
  }
  
  .story-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
  
  .story-card-1 {
    grid-row: auto;
  }
}
</style>
