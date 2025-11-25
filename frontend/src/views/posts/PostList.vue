<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useAppStore } from '@/stores/app'
import PostCard from '@/components/common/PostCard.vue'
import gsap from 'gsap'

const store = useAppStore()
const searchQuery = ref('')
const selectedCategory = ref<number | null>(null)

const filteredPosts = computed(() => {
  let posts = store.posts
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    posts = posts.filter(p => 
      p.title.toLowerCase().includes(query) ||
      p.summary?.toLowerCase().includes(query)
    )
  }
  
  return posts
})

onMounted(async () => {
  await Promise.all([
    store.fetchPosts(0, 20),
    store.fetchCategories()
  ])
  
  // Animate page entrance
  gsap.fromTo('.page-header', 
    { opacity: 0, y: 50 },
    { opacity: 1, y: 0, duration: 0.8, ease: 'power3.out' }
  )
  
  gsap.fromTo('.posts-grid .post-card',
    { opacity: 0, y: 30 },
    { opacity: 1, y: 0, duration: 0.6, stagger: 0.1, ease: 'power3.out', delay: 0.3 }
  )
})

const loadMore = async () => {
  await store.fetchPosts(store.currentPage + 1, 10)
}
</script>

<template>
  <div class="posts-page">
    <!-- Page Header -->
    <section class="page-header">
      <div class="container">
        <h1>
          <span class="page-icon">📝</span>
          All Posts
        </h1>
        <p class="page-desc">
          Discover stories, ideas, and insights from our writers
        </p>
        
        <!-- Search & Filters -->
        <div class="filters">
          <div class="search-box">
            <input 
              v-model="searchQuery"
              type="text" 
              class="input" 
              placeholder="Search posts..."
            />
            <span class="search-icon">🔍</span>
          </div>
          
          <div class="category-filters">
            <button 
              class="filter-btn"
              :class="{ 'active': selectedCategory === null }"
              @click="selectedCategory = null"
            >
              All
            </button>
            <button 
              v-for="category in store.categories"
              :key="category.id"
              class="filter-btn"
              :class="{ 'active': selectedCategory === category.id }"
              @click="selectedCategory = category.id"
            >
              {{ category.name }}
            </button>
          </div>
        </div>
      </div>
    </section>
    
    <!-- Posts Grid -->
    <section class="posts-section section">
      <div class="container">
        <!-- Loading State -->
        <div v-if="store.isLoading" class="loading">
          <div class="loading-spinner"></div>
          <p>Loading posts...</p>
        </div>
        
        <!-- Empty State -->
        <div v-else-if="filteredPosts.length === 0" class="empty-state">
          <span class="empty-icon">📭</span>
          <h3>No posts found</h3>
          <p>Try adjusting your search or filters</p>
        </div>
        
        <!-- Posts Grid -->
        <div v-else class="posts-grid">
          <PostCard 
            v-for="post in filteredPosts"
            :key="post.id"
            :post="post"
          />
        </div>
        
        <!-- Load More -->
        <div v-if="store.posts.length < store.totalPosts" class="load-more">
          <button class="btn btn-secondary" @click="loadMore">
            Load More
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.posts-page {
  min-height: 100vh;
}

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
  margin-bottom: 40px;
}

.filters {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.search-box {
  position: relative;
}

.search-box .input {
  padding-right: 50px;
}

.search-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.25rem;
}

.category-filters {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.filter-btn {
  padding: 8px 20px;
  border-radius: var(--radius-full);
  border: 2px solid var(--bg-card);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-btn:hover,
.filter-btn.active {
  border-color: var(--color-primary);
  background: var(--color-primary);
  color: white;
}

.posts-section {
  padding: 40px 0 80px;
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

.empty-state h3 {
  font-size: 1.5rem;
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--text-secondary);
}

.load-more {
  text-align: center;
  margin-top: 48px;
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 2rem;
  }
  
  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
