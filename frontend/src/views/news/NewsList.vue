<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import newsApi, { type News, type PaginatedResponse } from '@/api/news'

const news = ref<News[]>([])
const hotNews = ref<News[]>([])
const isLoading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const searchKeyword = ref('')
const selectedCategory = ref<string>('')

const categories = [
  { value: '', label: 'All' },
  { value: 'TECHNOLOGY', label: '科技' },
  { value: 'FINANCE', label: '财经' },
  { value: 'POLITICS', label: '政治' },
  { value: 'SPORTS', label: '体育' },
  { value: 'ENTERTAINMENT', label: '娱乐' },
  { value: 'HEALTH', label: '健康' },
  { value: 'SCIENCE', label: '科学' },
  { value: 'WORLD', label: '国际' },
  { value: 'DOMESTIC', label: '国内' },
]

const fetchNews = async () => {
  isLoading.value = true
  try {
    let response: { data: PaginatedResponse<News> }
    
    if (searchKeyword.value) {
      response = await newsApi.search(searchKeyword.value, currentPage.value, 10)
    } else if (selectedCategory.value) {
      response = await newsApi.listByCategory(selectedCategory.value, currentPage.value, 10)
    } else {
      response = await newsApi.list(currentPage.value, 10)
    }
    
    news.value = response.data.records || []
    totalPages.value = response.data.pages
  } catch (error) {
    console.error('Failed to fetch news:', error)
  } finally {
    isLoading.value = false
  }
}

const fetchHotNews = async () => {
  try {
    const response = await newsApi.listHot(5)
    hotNews.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch hot news:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 0
  fetchNews()
}

const handleCategoryChange = () => {
  currentPage.value = 0
  searchKeyword.value = ''
  fetchNews()
}

const goToPage = (page: number) => {
  currentPage.value = page
  fetchNews()
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

onMounted(async () => {
  await Promise.all([fetchNews(), fetchHotNews()])
})
</script>

<template>
  <div class="news-page">
    <div class="container">
      <header class="page-header">
        <h1>🔥 热点新闻</h1>
        <p>实时追踪热门资讯</p>
      </header>
      
      <!-- Hot News Banner -->
      <section v-if="hotNews.length > 0" class="hot-news-section">
        <h2>🏆 今日热点</h2>
        <div class="hot-news-grid">
          <RouterLink
            v-for="(item, index) in hotNews"
            :key="item.id"
            :to="`/news/${item.id}`"
            class="hot-news-item"
            :class="{ featured: index === 0 }"
          >
            <div class="hot-rank">{{ index + 1 }}</div>
            <div class="hot-content">
              <h3>{{ item.title }}</h3>
              <span class="hot-source">{{ item.source }}</span>
            </div>
          </RouterLink>
        </div>
      </section>
      
      <!-- Search and Filter -->
      <div class="filter-section">
        <div class="search-box">
          <input 
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索新闻..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        
        <div class="category-filter">
          <select v-model="selectedCategory" @change="handleCategoryChange" class="category-select">
            <option v-for="cat in categories" :key="cat.value" :value="cat.value">
              {{ cat.label }}
            </option>
          </select>
        </div>
      </div>
      
      <!-- News List -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="news.length === 0" class="empty-state">
        <span class="empty-icon">📰</span>
        <p>暂无新闻</p>
      </div>
      
      <div v-else class="news-list">
        <RouterLink
          v-for="item in news"
          :key="item.id"
          :to="`/news/${item.id}`"
          class="news-card"
        >
          <div v-if="item.thumbnail" class="news-image">
            <img :src="item.thumbnail" :alt="item.title" />
          </div>
          <div class="news-content">
            <div class="news-meta">
              <span class="news-category">{{ item.category }}</span>
              <span class="news-date">{{ formatDate(item.publishedAt) }}</span>
            </div>
            <h3 class="news-title">{{ item.title }}</h3>
            <p v-if="item.summary" class="news-summary">{{ item.summary }}</p>
            <div class="news-footer">
              <span class="news-source">{{ item.source }}</span>
              <span class="news-views">👁 {{ item.viewCount }}</span>
            </div>
          </div>
        </RouterLink>
      </div>
      
      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          class="page-btn"
          :disabled="currentPage === 0"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <button 
          class="page-btn"
          :disabled="currentPage >= totalPages - 1"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.news-page {
  min-height: 100vh;
  padding: 100px 0 60px;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

/* Hot News Section */
.hot-news-section {
  margin-bottom: 48px;
}

.hot-news-section h2 {
  font-size: 1.5rem;
  margin-bottom: 24px;
}

.hot-news-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-news-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all 0.3s ease;
}

.hot-news-item:hover {
  transform: translateX(8px);
  background: rgba(255, 255, 255, 0.1);
}

.hot-news-item.featured {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(236, 72, 153, 0.2));
}

.hot-rank {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.hot-news-item:nth-child(1) .hot-rank { background: #ef4444; }
.hot-news-item:nth-child(2) .hot-rank { background: #f97316; }
.hot-news-item:nth-child(3) .hot-rank { background: #fbbf24; }

.hot-content h3 {
  font-size: 1rem;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.hot-source {
  font-size: 0.75rem;
  color: var(--text-muted);
}

/* Filter Section */
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 200px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  color: var(--text-primary);
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.search-btn {
  padding: 12px 24px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  cursor: pointer;
  font-weight: 500;
}

.category-select {
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
}

/* News List */
.news-list {
  display: grid;
  gap: 24px;
}

.news-card {
  display: flex;
  gap: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  text-decoration: none;
  transition: all 0.3s ease;
}

.news-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.news-image {
  width: 200px;
  flex-shrink: 0;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.news-content {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.news-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.news-category {
  padding: 4px 12px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
}

.news-date {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.news-title {
  font-size: 1.25rem;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.news-summary {
  color: var(--text-secondary);
  font-size: 0.9rem;
  line-height: 1.6;
  flex: 1;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.news-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 0.875rem;
  color: var(--text-muted);
}

/* Loading and Empty */
.loading,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 16px;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 48px;
}

.page-btn {
  padding: 10px 20px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .news-card {
    flex-direction: column;
  }
  
  .news-image {
    width: 100%;
    height: 200px;
  }
  
  .filter-section {
    flex-direction: column;
  }
}
</style>
