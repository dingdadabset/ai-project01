<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import newsApi, { type News, type NewsRequest } from '@/api/news'

const news = ref<News[]>([])
const showModal = ref(false)
const editingNews = ref<News | null>(null)
const isSubmitting = ref(false)
const isLoading = ref(true)
const isFetching = ref(false)

const categories = [
  'TECHNOLOGY', 'FINANCE', 'POLITICS', 'SPORTS',
  'ENTERTAINMENT', 'HEALTH', 'SCIENCE', 'WORLD', 'DOMESTIC', 'OTHER'
]

const formData = reactive<NewsRequest & { isHot?: boolean; hotScore?: number }>({
  title: '',
  summary: '',
  content: '',
  source: '',
  sourceUrl: '',
  thumbnail: '',
  category: 'OTHER',
  isHot: false,
  hotScore: 0
})

const fetchNews = async () => {
  isLoading.value = true
  try {
    const response = await newsApi.list(0, 50)
    news.value = response.data.records || []
  } catch (error) {
    console.error('Failed to fetch news:', error)
  } finally {
    isLoading.value = false
  }
}

const fetchExternalNews = async () => {
  isFetching.value = true
  try {
    const response = await newsApi.fetchExternal()
    alert(`成功拉取 ${response.data.count} 条新闻`)
    await fetchNews()
  } catch (error) {
    console.error('Failed to fetch external news:', error)
    alert('拉取外部新闻失败')
  } finally {
    isFetching.value = false
  }
}

onMounted(() => {
  fetchNews()
})

const openCreateModal = () => {
  editingNews.value = null
  Object.assign(formData, {
    title: '',
    summary: '',
    content: '',
    source: '',
    sourceUrl: '',
    thumbnail: '',
    category: 'OTHER',
    isHot: false,
    hotScore: 0
  })
  showModal.value = true
}

const openEditModal = (item: News) => {
  editingNews.value = item
  Object.assign(formData, {
    title: item.title,
    summary: item.summary || '',
    content: item.content || '',
    source: item.source || '',
    sourceUrl: item.sourceUrl || '',
    thumbnail: item.thumbnail || '',
    category: item.category,
    isHot: item.isHot,
    hotScore: item.hotScore
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingNews.value = null
}

const submitNews = async () => {
  if (!formData.title.trim()) {
    alert('请输入标题')
    return
  }
  
  isSubmitting.value = true
  try {
    if (editingNews.value) {
      await newsApi.update(editingNews.value.id, formData)
    } else {
      await newsApi.create(formData)
    }
    await fetchNews()
    closeModal()
  } catch (error) {
    console.error('Failed to save news:', error)
    alert('保存失败')
  } finally {
    isSubmitting.value = false
  }
}

const deleteNews = async (id: number) => {
  if (confirm('确定要删除这条新闻吗?')) {
    await newsApi.delete(id)
    await fetchNews()
  }
}

const toggleHot = async (item: News) => {
  await newsApi.setHot(item.id, !item.isHot, item.hotScore)
  await fetchNews()
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="news-manager">
    <header class="page-header">
      <div>
        <h1>📰 新闻管理</h1>
        <p>管理热点新闻内容</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="fetchExternalNews" :disabled="isFetching">
          {{ isFetching ? '拉取中...' : '🔄 拉取外部新闻' }}
        </button>
        <button class="btn btn-primary" @click="openCreateModal">
          + 添加新闻
        </button>
      </div>
    </header>
    
    <div v-if="isLoading" class="loading">加载中...</div>
    
    <div v-else class="news-table">
      <table>
        <thead>
          <tr>
            <th>标题</th>
            <th>分类</th>
            <th>来源</th>
            <th>热门</th>
            <th>浏览</th>
            <th>日期</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in news" :key="item.id">
            <td>
              <div class="news-title">{{ item.title }}</div>
            </td>
            <td>
              <span class="category">{{ item.category }}</span>
            </td>
            <td>{{ item.source || '-' }}</td>
            <td>
              <button 
                class="hot-btn" 
                :class="{ active: item.isHot }"
                @click="toggleHot(item)"
              >
                {{ item.isHot ? '🔥' : '❄️' }}
              </button>
            </td>
            <td>{{ item.viewCount }}</td>
            <td>{{ formatDate(item.publishedAt) }}</td>
            <td>
              <div class="actions">
                <button class="action-btn edit" @click="openEditModal(item)">编辑</button>
                <button class="action-btn delete" @click="deleteNews(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingNews ? '编辑新闻' : '添加新闻' }}</h2>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label>标题 *</label>
            <input v-model="formData.title" type="text" class="form-input" placeholder="新闻标题" />
          </div>
          
          <div class="form-group">
            <label>摘要</label>
            <textarea v-model="formData.summary" class="form-input" rows="2" placeholder="新闻摘要"></textarea>
          </div>
          
          <div class="form-group">
            <label>内容</label>
            <textarea v-model="formData.content" class="form-input" rows="6" placeholder="新闻内容"></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>来源</label>
              <input v-model="formData.source" type="text" class="form-input" placeholder="新闻来源" />
            </div>
            <div class="form-group">
              <label>分类</label>
              <select v-model="formData.category" class="form-input">
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>
          </div>
          
          <div class="form-group">
            <label>原文链接</label>
            <input v-model="formData.sourceUrl" type="text" class="form-input" placeholder="https://..." />
          </div>
          
          <div class="form-group">
            <label>封面图片</label>
            <input v-model="formData.thumbnail" type="text" class="form-input" placeholder="图片URL" />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>
                <input type="checkbox" v-model="formData.isHot" />
                设为热门
              </label>
            </div>
            <div class="form-group">
              <label>热度分数</label>
              <input v-model.number="formData.hotScore" type="number" class="form-input" min="0" />
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="submitNews" :disabled="isSubmitting">
            {{ isSubmitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.news-manager {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.news-table {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

th, td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

th {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.news-title {
  font-weight: 500;
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category {
  padding: 4px 12px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
}

.hot-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: none;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
}

.action-btn.edit {
  background: var(--color-primary);
  color: white;
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--text-muted);
  cursor: pointer;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-secondary);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 1rem;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.btn {
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
