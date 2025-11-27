<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import externalToolApi, { type ExternalTool } from '@/api/externalTools'
import gsap from 'gsap'

const tools = ref<ExternalTool[]>([])
const isLoading = ref(true)
const searchKeyword = ref('')
const selectedCategory = ref<string>('')
const showAddModal = ref(false)
const editingTool = ref<ExternalTool | null>(null)

// Form data for add/edit
const formData = ref({
  name: '',
  description: '',
  url: '',
  icon: '',
  iconBgColor: '#6366F1',
  category: 'OTHER',
  displayOrder: 0
})

const categories = [
  { value: '', label: '全部' },
  { value: 'PRODUCTIVITY', label: '🛠️ 生产力' },
  { value: 'SEARCH', label: '🔍 搜索' },
  { value: 'SOCIAL', label: '💬 社交' },
  { value: 'DEVELOPMENT', label: '💻 开发' },
  { value: 'ENTERTAINMENT', label: '🎮 娱乐' },
  { value: 'FINANCE', label: '💰 财经' },
  { value: 'NEWS', label: '📰 新闻' },
  { value: 'OTHER', label: '📦 其他' }
]

const categoryIcons: Record<string, string> = {
  PRODUCTIVITY: '🛠️',
  SEARCH: '🔍',
  SOCIAL: '💬',
  DEVELOPMENT: '💻',
  ENTERTAINMENT: '🎮',
  FINANCE: '💰',
  NEWS: '📰',
  OTHER: '📦'
}

// Icon presets for common tools
const iconPresets = [
  { name: 'notion', icon: '📝', color: '#000000' },
  { name: 'baidu', icon: '🔵', color: '#2932E1' },
  { name: 'google', icon: '🔴', color: '#4285F4' },
  { name: 'github', icon: '🐱', color: '#24292E' },
  { name: 'stackoverflow', icon: '📚', color: '#F48024' },
  { name: 'bilibili', icon: '📺', color: '#FB7299' },
  { name: 'youtube', icon: '▶️', color: '#FF0000' },
  { name: 'twitter', icon: '🐦', color: '#1DA1F2' },
  { name: 'custom', icon: '🔗', color: '#6366F1' }
]

const filteredTools = computed(() => {
  let result = tools.value
  
  if (selectedCategory.value) {
    result = result.filter(t => t.category === selectedCategory.value)
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t => 
      t.name.toLowerCase().includes(keyword) ||
      (t.description && t.description.toLowerCase().includes(keyword))
    )
  }
  
  return result
})

// Group tools by category
const toolsByCategory = computed(() => {
  const grouped: Record<string, ExternalTool[]> = {}
  for (const tool of filteredTools.value) {
    if (!grouped[tool.category]) {
      grouped[tool.category] = []
    }
    grouped[tool.category].push(tool)
  }
  return grouped
})

const fetchTools = async () => {
  isLoading.value = true
  try {
    const response = await externalToolApi.listActive()
    tools.value = response.data || []
    
    // If no tools, try to initialize default ones
    if (tools.value.length === 0) {
      await externalToolApi.init()
      const refreshResponse = await externalToolApi.listActive()
      tools.value = refreshResponse.data || []
    }
  } catch (error) {
    console.error('Failed to fetch tools:', error)
  } finally {
    isLoading.value = false
  }
}

const openLink = (tool: ExternalTool, event: MouseEvent) => {
  const target = event.currentTarget as HTMLElement
  
  // Create ripple effect
  const ripple = document.createElement('span')
  ripple.className = 'ripple-effect'
  const rect = target.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  ripple.style.width = ripple.style.height = size + 'px'
  ripple.style.left = event.clientX - rect.left - size / 2 + 'px'
  ripple.style.top = event.clientY - rect.top - size / 2 + 'px'
  target.appendChild(ripple)
  
  // Animate card press
  gsap.to(target, {
    scale: 0.95,
    duration: 0.1,
    ease: 'power2.out',
    onComplete: () => {
      gsap.to(target, {
        scale: 1,
        duration: 0.2,
        ease: 'elastic.out(1, 0.5)'
      })
    }
  })
  
  // Open in new window
  setTimeout(() => {
    window.open(tool.url, '_blank', 'noopener,noreferrer')
    ripple.remove()
  }, 200)
}

const getIconDisplay = (tool: ExternalTool) => {
  // Check if icon is an emoji or preset name
  const preset = iconPresets.find(p => p.name === tool.icon)
  if (preset) {
    return preset.icon
  }
  // Return the icon directly if it's an emoji or custom icon
  return tool.icon || '🔗'
}

const openAddModal = () => {
  editingTool.value = null
  formData.value = {
    name: '',
    description: '',
    url: '',
    icon: 'custom',
    iconBgColor: '#6366F1',
    category: 'OTHER',
    displayOrder: 0
  }
  showAddModal.value = true
}

const openEditModal = (tool: ExternalTool) => {
  editingTool.value = tool
  formData.value = {
    name: tool.name,
    description: tool.description || '',
    url: tool.url,
    icon: tool.icon || 'custom',
    iconBgColor: tool.iconBgColor || '#6366F1',
    category: tool.category,
    displayOrder: tool.displayOrder
  }
  showAddModal.value = true
}

const saveTool = async () => {
  try {
    if (editingTool.value) {
      await externalToolApi.update(editingTool.value.id, formData.value)
    } else {
      await externalToolApi.create(formData.value)
    }
    showAddModal.value = false
    await fetchTools()
  } catch (error) {
    console.error('Failed to save tool:', error)
  }
}

const deleteTool = async (tool: ExternalTool) => {
  if (!confirm(`确定要删除 "${tool.name}" 吗？`)) return
  
  try {
    await externalToolApi.delete(tool.id)
    await fetchTools()
  } catch (error) {
    console.error('Failed to delete tool:', error)
  }
}

const animateToolCards = () => {
  gsap.fromTo('.tool-card',
    { opacity: 0, y: 30, scale: 0.9 },
    { 
      opacity: 1, 
      y: 0, 
      scale: 1,
      duration: 0.5,
      stagger: 0.05,
      ease: 'back.out(1.2)'
    }
  )
}

onMounted(async () => {
  await fetchTools()
  setTimeout(animateToolCards, 100)
})
</script>

<template>
  <div class="tools-page">
    <div class="container">
      <header class="page-header">
        <h1>🧰 外部工具</h1>
        <p>快速访问常用工具和网站</p>
      </header>
      
      <!-- Search and Filter -->
      <div class="filter-section">
        <div class="search-box">
          <input 
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索工具..."
          />
        </div>
        
        <div class="category-filter">
          <select v-model="selectedCategory" class="category-select">
            <option v-for="cat in categories" :key="cat.value" :value="cat.value">
              {{ cat.label }}
            </option>
          </select>
        </div>
        
        <button class="add-btn" @click="openAddModal">
          ➕ 添加工具
        </button>
      </div>
      
      <!-- Loading State -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="filteredTools.length === 0" class="empty-state">
        <span class="empty-icon">🧰</span>
        <p>暂无工具</p>
        <button class="btn btn-primary" @click="openAddModal">添加第一个工具</button>
      </div>
      
      <!-- Tools Grid by Category -->
      <div v-else class="tools-container">
        <div 
          v-for="(categoryTools, category) in toolsByCategory" 
          :key="category"
          class="category-section"
        >
          <h2 class="category-title">
            {{ categoryIcons[category] || '📦' }} {{ categories.find(c => c.value === category)?.label.split(' ')[1] || category }}
          </h2>
          
          <div class="tools-grid">
            <div
              v-for="tool in categoryTools"
              :key="tool.id"
              class="tool-card"
              :style="{ '--icon-bg': tool.iconBgColor || '#6366F1' }"
              @click="openLink(tool, $event)"
            >
              <div class="tool-icon">
                {{ getIconDisplay(tool) }}
              </div>
              <div class="tool-info">
                <h3 class="tool-name">{{ tool.name }}</h3>
                <p v-if="tool.description" class="tool-desc">{{ tool.description }}</p>
              </div>
              <div class="tool-actions" @click.stop>
                <button class="action-btn edit-btn" @click="openEditModal(tool)" title="编辑">
                  ✏️
                </button>
                <button class="action-btn delete-btn" @click="deleteTool(tool)" title="删除">
                  🗑️
                </button>
              </div>
              <div class="tool-arrow">→</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Add/Edit Modal -->
      <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
        <div class="modal">
          <div class="modal-header">
            <h2>{{ editingTool ? '编辑工具' : '添加工具' }}</h2>
            <button class="close-btn" @click="showAddModal = false">✕</button>
          </div>
          
          <div class="modal-body">
            <div class="form-group">
              <label>名称 *</label>
              <input v-model="formData.name" type="text" placeholder="例如: Notion" required />
            </div>
            
            <div class="form-group">
              <label>描述</label>
              <input v-model="formData.description" type="text" placeholder="简短描述" />
            </div>
            
            <div class="form-group">
              <label>链接 *</label>
              <input v-model="formData.url" type="url" placeholder="https://..." required />
            </div>
            
            <div class="form-group">
              <label>图标</label>
              <div class="icon-picker">
                <button
                  v-for="preset in iconPresets"
                  :key="preset.name"
                  class="icon-option"
                  :class="{ active: formData.icon === preset.name }"
                  :style="{ background: preset.color }"
                  @click="formData.icon = preset.name; formData.iconBgColor = preset.color"
                >
                  {{ preset.icon }}
                </button>
              </div>
            </div>
            
            <div class="form-group">
              <label>分类</label>
              <select v-model="formData.category">
                <option v-for="cat in categories.slice(1)" :key="cat.value" :value="cat.value">
                  {{ cat.label }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label>排序 (数字越小越靠前)</label>
              <input v-model.number="formData.displayOrder" type="number" min="0" />
            </div>
          </div>
          
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="showAddModal = false">取消</button>
            <button class="btn btn-primary" @click="saveTool">
              {{ editingTool ? '保存' : '添加' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tools-page {
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

/* Filter Section */
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
  align-items: center;
}

.search-box {
  flex: 1;
  min-width: 200px;
}

.search-input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.category-select {
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
}

.add-btn {
  padding: 12px 24px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

/* Category Section */
.category-section {
  margin-bottom: 48px;
}

.category-title {
  font-size: 1.5rem;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* Tools Grid */
.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* Tool Card */
.tool-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  border: 1px solid transparent;
}

.tool-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--icon-bg) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.3);
  border-color: var(--icon-bg);
}

.tool-card:hover::before {
  opacity: 0.1;
}

.tool-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  background: var(--icon-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  flex-shrink: 0;
  transition: all 0.3s ease;
  z-index: 1;
}

.tool-card:hover .tool-icon {
  transform: scale(1.1) rotate(5deg);
}

.tool-info {
  flex: 1;
  min-width: 0;
  z-index: 1;
}

.tool-name {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-primary);
}

.tool-desc {
  font-size: 0.85rem;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tool-arrow {
  font-size: 1.25rem;
  color: var(--text-muted);
  transition: all 0.3s ease;
  z-index: 1;
}

.tool-card:hover .tool-arrow {
  color: var(--icon-bg);
  transform: translateX(4px);
}

.tool-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 2;
}

.tool-card:hover .tool-actions {
  opacity: 1;
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: rgba(0, 0, 0, 0.5);
  cursor: pointer;
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.7);
}

/* Ripple Effect */
.tool-card :deep(.ripple-effect) {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(4);
    opacity: 0;
  }
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

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal {
  width: 100%;
  max-width: 500px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  font-size: 1.25rem;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 1rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.icon-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.icon-option {
  width: 44px;
  height: 44px;
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 1.25rem;
  transition: all 0.2s ease;
}

.icon-option.active {
  border-color: white;
  transform: scale(1.1);
}

.icon-option:hover {
  transform: scale(1.05);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-primary:hover {
  background: var(--color-primary-dark, #5558E3);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
  }
  
  .search-box {
    width: 100%;
  }
  
  .tools-grid {
    grid-template-columns: 1fr;
  }
}
</style>
