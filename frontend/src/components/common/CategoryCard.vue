<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import type { Category } from '@/types'
import gsap from 'gsap'

const props = defineProps<{
  category: Category
}>()

const cardRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (cardRef.value) {
    cardRef.value.addEventListener('mouseenter', handleMouseEnter)
    cardRef.value.addEventListener('mouseleave', handleMouseLeave)
  }
})

const handleMouseEnter = () => {
  if (!cardRef.value) return
  
  gsap.to(cardRef.value, {
    scale: 1.05,
    y: -8,
    duration: 0.3,
    ease: 'power2.out'
  })
  
  gsap.to('.category-icon', {
    scale: 1.2,
    rotateZ: 10,
    duration: 0.3,
    ease: 'back.out(1.7)'
  })
}

const handleMouseLeave = () => {
  if (!cardRef.value) return
  
  gsap.to(cardRef.value, {
    scale: 1,
    y: 0,
    duration: 0.3,
    ease: 'power2.out'
  })
  
  gsap.to('.category-icon', {
    scale: 1,
    rotateZ: 0,
    duration: 0.3,
    ease: 'power2.out'
  })
}

// Get category icon based on name
const getCategoryIcon = (name: string) => {
  const icons: Record<string, string> = {
    '技术': '💻',
    '生活': '🌱',
    '旅行': '✈️',
    '设计': '🎨',
    '编程': '⌨️',
    'Tech': '💻',
    'Life': '🌱',
    'Travel': '✈️'
  }
  return icons[name] || '📁'
}
</script>

<template>
  <RouterLink
    ref="cardRef"
    :to="`/categories/${category.id}`"
    class="category-card"
  >
    <div class="card-bg"></div>
    
    <div class="card-content">
      <span class="category-icon">
        {{ getCategoryIcon(category.name) }}
      </span>
      
      <h3 class="category-name">{{ category.name }}</h3>
      
      <p v-if="category.description" class="category-desc">
        {{ category.description }}
      </p>
      
      <div class="category-stats">
        <span class="post-count">
          {{ category.postCount }} posts
        </span>
      </div>
    </div>
    
    <div class="card-arrow">→</div>
  </RouterLink>
</template>

<style scoped>
.category-card {
  position: relative;
  display: block;
  padding: 32px;
  border-radius: var(--radius-lg);
  background: var(--bg-card);
  text-decoration: none;
  color: inherit;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.category-card:hover {
  box-shadow: 0 20px 40px rgba(99, 102, 241, 0.15);
}

.card-bg {
  position: absolute;
  top: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle at top right, rgba(99, 102, 241, 0.2) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.category-card:hover .card-bg {
  opacity: 1;
}

.card-content {
  position: relative;
  z-index: 1;
}

.category-icon {
  display: inline-block;
  font-size: 3rem;
  margin-bottom: 16px;
}

.category-name {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.category-card:hover .category-name {
  color: var(--color-primary);
}

.category-desc {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.category-stats {
  display: flex;
  align-items: center;
}

.post-count {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.card-arrow {
  position: absolute;
  right: 24px;
  bottom: 24px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
}

.category-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
}
</style>
