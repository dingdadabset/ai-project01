<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import tagApi from '@/api/tags'
import postApi from '@/api/posts'
import type { Post, Tag } from '@/types'
import PostCard from '@/components/common/PostCard.vue'

const route = useRoute()
const posts = ref<Post[]>([])
const tag = ref<Tag | null>(null)
const isLoading = ref(true)

const tagSlug = computed(() => route.params.slug as string)

onMounted(async () => {
  try {
    // First get the tag by slug
    const tagRes = await tagApi.getBySlug(tagSlug.value)
    tag.value = tagRes.data
    
    // Then fetch posts by tag ID
    if (tag.value?.id) {
      const postsRes = await postApi.listByTag(tag.value.id, 0, 20)
      posts.value = postsRes.data.records || []
    }
  } catch (error) {
    console.error('Failed to load tag:', error)
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="tag-posts-page">
    <section class="page-header">
      <div class="container">
        <h1 v-if="tag">
          <span class="page-icon">🏷️</span>
          #{{ tag.name }}
        </h1>
        <p class="page-desc">
          {{ tag?.postCount || 0 }} posts with this tag
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
          <h3>No posts with this tag yet</h3>
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
