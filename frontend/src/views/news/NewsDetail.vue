<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import newsApi, { type News } from '@/api/news'

const route = useRoute()
const news = ref<News | null>(null)
const isLoading = ref(true)

const fetchNews = async () => {
  const id = Number(route.params.id)
  if (!id) return
  
  isLoading.value = true
  try {
    const response = await newsApi.getById(id)
    news.value = response.data
  } catch (error) {
    console.error('Failed to fetch news:', error)
  } finally {
    isLoading.value = false
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchNews()
})
</script>

<template>
  <div class="news-detail-page">
    <div class="container">
      <!-- Loading State -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- News Content -->
      <article v-else-if="news" class="news-article">
        <header class="article-header">
          <div class="article-meta">
            <span class="category">{{ news.category }}</span>
            <span class="date">{{ formatDate(news.publishedAt) }}</span>
          </div>
          
          <h1 class="article-title">{{ news.title }}</h1>
          
          <div v-if="news.summary" class="article-summary">
            {{ news.summary }}
          </div>
          
          <div class="article-info">
            <span class="source">来源: {{ news.source }}</span>
            <span class="views">👁 {{ news.viewCount }} 次阅读</span>
          </div>
        </header>
        
        <div v-if="news.thumbnail" class="article-image">
          <img :src="news.thumbnail" :alt="news.title" />
        </div>
        
        <div class="article-content" v-html="news.content"></div>
        
        <div v-if="news.sourceUrl" class="article-source">
          <a :href="news.sourceUrl" target="_blank" rel="noopener noreferrer">
            查看原文 →
          </a>
        </div>
        
        <footer class="article-footer">
          <RouterLink to="/news" class="back-link">
            ← 返回新闻列表
          </RouterLink>
        </footer>
      </article>
      
      <!-- Not Found -->
      <div v-else class="not-found">
        <span class="not-found-icon">📰</span>
        <h2>新闻未找到</h2>
        <RouterLink to="/news" class="btn btn-primary">
          返回新闻列表
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.news-detail-page {
  min-height: 100vh;
  padding: 100px 0 60px;
}

.loading,
.not-found {
  text-align: center;
  padding: 60px 20px;
}

.not-found-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 16px;
}

.news-article {
  max-width: 800px;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 32px;
}

.article-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.category {
  padding: 6px 16px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 500;
}

.date {
  color: var(--text-muted);
  font-size: 0.875rem;
  display: flex;
  align-items: center;
}

.article-title {
  font-size: 2.5rem;
  line-height: 1.3;
  margin-bottom: 16px;
}

.article-summary {
  font-size: 1.25rem;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 24px;
  padding-left: 16px;
  border-left: 4px solid var(--color-primary);
}

.article-info {
  display: flex;
  gap: 24px;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.article-image {
  margin-bottom: 32px;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: auto;
}

.article-content {
  font-size: 1.1rem;
  line-height: 1.8;
  color: var(--text-secondary);
}

.article-content :deep(h2) {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin: 32px 0 16px;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.article-content :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
}

.article-source {
  margin-top: 32px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  text-align: center;
}

.article-source a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.article-source a:hover {
  text-decoration: underline;
}

.article-footer {
  margin-top: 48px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.back-link {
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .article-title {
    font-size: 1.75rem;
  }
  
  .article-summary {
    font-size: 1rem;
  }
}
</style>
