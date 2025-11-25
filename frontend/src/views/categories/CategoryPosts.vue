<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import postApi from '@/api/posts'
import categoryApi from '@/api/categories'
import type { Post, Category } from '@/types'
import PostCard from '@/components/common/PostCard.vue'

const route = useRoute()
const posts = ref<Post[]>([])
const category = ref<Category | null>(null)
const isLoading = ref(true)

const categoryId = computed(() => Number(route.params.id))

onMounted(async () => {
  try {
    const [categoryRes, postsRes] = await Promise.all([
      categoryApi.getById(categoryId.value),
      postApi.listByCategory(categoryId.value, 0, 20)
    ])
    category.value = categoryRes.data
    posts.value = postsRes.data.records || []
  } catch (error) {
    console.error('Failed to load category posts:', error)
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="category-posts-page">
    <section class="page-header">
      <div class="container">
        <h1 v-if="category">
          <span class="page-icon">📁</span>
          {{ category.name }}
        </h1>
        <p v-if="category?.description" class="page-desc">
          {{ category.description }}
        </p>
      </div>
    </section>
    
    <section class="section">
      <div class="container">
        <div v-if="isLoading" class="loading">
          <div class="loading-spinner"></div>
        </div>
        
        <div v-else-if="posts.length === 0" class="empty-state">
          <span class="empty-icon">📭</span>
          <h3>No posts in this category</h3>
        </div>
        
        <div v-else class="posts-grid">
          <PostCard 
            v-for="post in posts"
            :key="post.id"
            :post="post"
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

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 32px;
}

.loading,
.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 24px;
}
</style>
