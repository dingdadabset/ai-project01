<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import type { Post } from '@/types'
import gsap from 'gsap'

const props = defineProps<{
  post: Post
  featured?: boolean
}>()

const cardRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (cardRef.value) {
    // Card tilt effect on hover
    cardRef.value.addEventListener('mousemove', handleMouseMove)
    cardRef.value.addEventListener('mouseleave', handleMouseLeave)
  }
})

const handleMouseMove = (e: MouseEvent) => {
  if (!cardRef.value) return
  
  const rect = cardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  
  const rotateX = ((y - centerY) / centerY) * -8
  const rotateY = ((x - centerX) / centerX) * 8
  
  gsap.to(cardRef.value, {
    rotateX,
    rotateY,
    scale: 1.02,
    duration: 0.3,
    ease: 'power2.out'
  })
  
  // Move glow effect
  gsap.to('.card-glow', {
    x: x - centerX,
    y: y - centerY,
    duration: 0.3
  })
}

const handleMouseLeave = () => {
  if (!cardRef.value) return
  
  gsap.to(cardRef.value, {
    rotateX: 0,
    rotateY: 0,
    scale: 1,
    duration: 0.5,
    ease: 'elastic.out(1, 0.5)'
  })
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<template>
  <article 
    ref="cardRef"
    class="post-card"
    :class="{ 'featured': featured }"
  >
    <RouterLink :to="`/posts/${post.slug}`" class="card-link">
      <!-- Card Glow Effect -->
      <div class="card-glow"></div>
      
      <!-- Thumbnail -->
      <div class="card-thumbnail">
        <img 
          :src="post.thumbnail || 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=800'" 
          :alt="post.title"
        />
        <div class="thumbnail-overlay"></div>
        
        <!-- Status Badge -->
        <span v-if="post.status === 'DRAFT'" class="status-badge draft">
          Draft
        </span>
      </div>
      
      <!-- Card Content -->
      <div class="card-content">
        <!-- Category & Date -->
        <div class="card-meta">
          <span v-if="post.categoryName" class="category">
            {{ post.categoryName }}
          </span>
          <span class="date">{{ formatDate(post.createdAt) }}</span>
        </div>
        
        <!-- Title -->
        <h3 class="card-title">{{ post.title }}</h3>
        
        <!-- Summary -->
        <p v-if="post.summary" class="card-summary">
          {{ post.summary }}
        </p>
        
        <!-- Tags -->
        <div v-if="post.tags?.length" class="card-tags">
          <span v-for="tag in post.tags.slice(0, 3)" :key="tag" class="tag">
            {{ tag }}
          </span>
        </div>
        
        <!-- Footer -->
        <div class="card-footer">
          <div class="author-info">
            <div class="author-avatar">
              {{ post.authorName?.charAt(0) || 'A' }}
            </div>
            <span class="author-name">{{ post.authorName || 'Anonymous' }}</span>
          </div>
          
          <div class="card-stats">
            <span class="stat">
              <span class="stat-icon">👁</span>
              {{ post.viewCount }}
            </span>
            <span class="stat">
              <span class="stat-icon">❤️</span>
              {{ post.likeCount }}
            </span>
            <span class="stat">
              <span class="stat-icon">💬</span>
              {{ post.commentCount }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- Read More Indicator -->
      <div class="read-more">
        <span>Read Story</span>
        <span class="arrow">→</span>
      </div>
    </RouterLink>
  </article>
</template>

<style scoped>
.post-card {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-card);
  transform-style: preserve-3d;
  perspective: 1000px;
  transition: box-shadow 0.3s ease;
}

.post-card:hover {
  box-shadow: 0 20px 40px rgba(99, 102, 241, 0.2);
}

.card-link {
  display: block;
  text-decoration: none;
  color: inherit;
  height: 100%;
}

.card-glow {
  position: absolute;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.4) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  opacity: 0;
  z-index: 1;
  transition: opacity 0.3s ease;
}

.post-card:hover .card-glow {
  opacity: 1;
}

.card-thumbnail {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.featured .card-thumbnail {
  height: 300px;
}

.card-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.post-card:hover .card-thumbnail img {
  transform: scale(1.1);
}

.thumbnail-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 50%, var(--bg-card) 100%);
}

.status-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.draft {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.card-content {
  padding: 24px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 0.875rem;
}

.category {
  color: var(--color-primary);
  font-weight: 500;
}

.date {
  color: var(--text-muted);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 12px;
  transition: color 0.3s ease;
}

.featured .card-title {
  font-size: 1.5rem;
}

.post-card:hover .card-title {
  color: var(--color-primary);
}

.card-summary {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured .card-summary {
  -webkit-line-clamp: 3;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.author-name {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.card-stats {
  display: flex;
  gap: 12px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.stat-icon {
  font-size: 0.875rem;
}

.read-more {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px;
  background: linear-gradient(to top, var(--bg-card) 0%, transparent 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
  color: var(--color-primary);
  opacity: 0;
  transform: translateY(100%);
  transition: all 0.3s ease;
}

.post-card:hover .read-more {
  opacity: 1;
  transform: translateY(0);
}

.read-more .arrow {
  transition: transform 0.3s ease;
}

.post-card:hover .read-more .arrow {
  transform: translateX(4px);
}
</style>
