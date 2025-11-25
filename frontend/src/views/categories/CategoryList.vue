<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import CategoryCard from '@/components/common/CategoryCard.vue'
import gsap from 'gsap'

const store = useAppStore()

onMounted(async () => {
  await store.fetchCategories()
  
  gsap.fromTo('.category-card',
    { opacity: 0, y: 30 },
    { opacity: 1, y: 0, duration: 0.6, stagger: 0.1, ease: 'power3.out' }
  )
})
</script>

<template>
  <div class="categories-page">
    <section class="page-header">
      <div class="container">
        <h1>
          <span class="page-icon">📚</span>
          Categories
        </h1>
        <p class="page-desc">Browse posts by category</p>
      </div>
    </section>
    
    <section class="section">
      <div class="container">
        <div class="categories-grid">
          <CategoryCard 
            v-for="category in store.categories"
            :key="category.id"
            :category="category"
          />
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

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 32px;
}
</style>
