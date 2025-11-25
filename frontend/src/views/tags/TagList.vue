<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAppStore } from '@/stores/app'
import gsap from 'gsap'

const store = useAppStore()

onMounted(async () => {
  await store.fetchTags()
  
  gsap.fromTo('.tag-item',
    { opacity: 0, scale: 0.8 },
    { opacity: 1, scale: 1, duration: 0.5, stagger: 0.05, ease: 'back.out(1.7)' }
  )
})
</script>

<template>
  <div class="tags-page">
    <section class="page-header">
      <div class="container">
        <h1>
          <span class="page-icon">🏷️</span>
          Tags
        </h1>
        <p class="page-desc">Explore posts by tags</p>
      </div>
    </section>
    
    <section class="section">
      <div class="container">
        <div class="tags-cloud">
          <RouterLink
            v-for="tag in store.tags"
            :key="tag.id"
            :to="`/tags/${tag.slug}`"
            class="tag-item"
          >
            <span class="tag-name"># {{ tag.name }}</span>
            <span class="tag-count">{{ tag.postCount }} posts</span>
          </RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page-header {
  padding: 80px 0 60px;
  text-align: center;
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.1) 0%, transparent 100%);
}

.page-header h1 {
  font-size: 3rem;
  margin-bottom: 16px;
}

.page-icon {
  margin-right: 16px;
}

.page-desc {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: center;
}

.tag-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 32px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
}

.tag-item:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 20px 40px rgba(99, 102, 241, 0.2);
}

.tag-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 8px;
}

.tag-count {
  font-size: 0.875rem;
  color: var(--text-muted);
}
</style>
