<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import gsap from 'gsap'

const store = useAppStore()

const stats = ref([
  { label: 'Total Posts', value: 0, icon: '📝', color: '#6366f1' },
  { label: 'Categories', value: 0, icon: '📁', color: '#ec4899' },
  { label: 'Tags', value: 0, icon: '🏷️', color: '#8b5cf6' },
  { label: 'Comments', value: 0, icon: '💬', color: '#10b981' }
])

onMounted(async () => {
  await Promise.all([
    store.fetchPosts(),
    store.fetchCategories(),
    store.fetchTags()
  ])
  
  stats.value[0].value = store.totalPosts
  stats.value[1].value = store.categories.length
  stats.value[2].value = store.tags.length
  
  // Animate stats
  gsap.fromTo('.stat-card',
    { opacity: 0, y: 20 },
    { opacity: 1, y: 0, duration: 0.5, stagger: 0.1, ease: 'power3.out' }
  )
})
</script>

<template>
  <div class="dashboard">
    <header class="page-header">
      <h1>Dashboard</h1>
      <p>Welcome to Halo Blog Admin</p>
    </header>
    
    <!-- Stats Grid -->
    <div class="stats-grid">
      <div 
        v-for="stat in stats" 
        :key="stat.label"
        class="stat-card"
        :style="{ '--accent-color': stat.color }"
      >
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-content">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </div>
    
    <!-- Recent Activity -->
    <section class="recent-section">
      <h2>Recent Posts</h2>
      <div class="recent-list">
        <div 
          v-for="post in store.posts.slice(0, 5)"
          :key="post.id"
          class="recent-item"
        >
          <div class="item-info">
            <h3>{{ post.title }}</h3>
            <p>{{ post.summary }}</p>
          </div>
          <span class="item-status" :class="post.status.toLowerCase()">
            {{ post.status }}
          </span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 48px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--accent-color);
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: 700;
  color: var(--accent-color);
}

.stat-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.recent-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.recent-section h2 {
  font-size: 1.25rem;
  margin-bottom: 24px;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  gap: 16px;
  flex-wrap: wrap;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-info h3 {
  font-size: 1rem;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-info p {
  color: var(--text-muted);
  font-size: 0.875rem;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-status {
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  white-space: nowrap;
  flex-shrink: 0;
}

.item-status.published {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.item-status.draft {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 16px;
  }
  
  .stat-card {
    padding: 16px;
    gap: 12px;
  }
  
  .stat-icon {
    font-size: 2rem;
  }
  
  .stat-value {
    font-size: 1.5rem;
  }
  
  .recent-section {
    padding: 16px;
  }
  
  .recent-item {
    padding: 12px;
  }
}
</style>
