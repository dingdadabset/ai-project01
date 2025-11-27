<template>
  <div class="theme-manager">
    <div class="page-header">
      <h1>Theme Management</h1>
      <div class="header-actions">
        <label class="btn btn-primary upload-btn">
          <input type="file" accept=".zip" @change="handleUpload" hidden>
          <span>📦 Upload Theme</span>
        </label>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading themes...</p>
    </div>

    <!-- Themes Grid -->
    <div v-else class="themes-grid">
      <div 
        v-for="theme in themes" 
        :key="theme.id" 
        class="theme-card"
        :class="{ 'active': theme.isActive, 'disabled': theme.status === 'DISABLED' }"
      >
        <div class="theme-screenshot">
          <img 
            v-if="theme.screenshot" 
            :src="getScreenshotUrl(theme.themeId)" 
            :alt="theme.name"
            @error="handleImageError"
          >
          <div v-else class="placeholder-screenshot">
            <span>🎨</span>
          </div>
          <div class="theme-status-badge" v-if="theme.isActive">
            ✓ Active
          </div>
          <div class="theme-status-badge disabled" v-else-if="theme.status === 'DISABLED'">
            Disabled
          </div>
        </div>

        <div class="theme-info">
          <h3>{{ theme.name }}</h3>
          <p class="theme-version">v{{ theme.version }}</p>
          <p class="theme-author" v-if="theme.author">
            By <a v-if="theme.authorUrl" :href="theme.authorUrl" target="_blank">{{ theme.author }}</a>
            <span v-else>{{ theme.author }}</span>
          </p>
          <p class="theme-description" v-if="theme.description">{{ theme.description }}</p>
        </div>

        <div class="theme-actions">
          <button 
            v-if="!theme.isActive && theme.status === 'ENABLED'" 
            class="btn btn-primary"
            @click="activateTheme(theme.themeId)"
            :disabled="activating"
          >
            Activate
          </button>
          <button 
            v-if="theme.status === 'ENABLED' && !theme.isActive" 
            class="btn btn-secondary"
            @click="disableTheme(theme.themeId)"
          >
            Disable
          </button>
          <button 
            v-if="theme.status === 'DISABLED'" 
            class="btn btn-secondary"
            @click="enableTheme(theme.themeId)"
          >
            Enable
          </button>
          <button 
            class="btn btn-outline"
            @click="openSettings(theme)"
          >
            Settings
          </button>
          <button 
            class="btn btn-outline"
            @click="previewTheme(theme.themeId)"
          >
            Preview
          </button>
          <button 
            v-if="!theme.isActive && theme.themeId !== 'default'" 
            class="btn btn-danger"
            @click="confirmDelete(theme)"
          >
            Delete
          </button>
        </div>
      </div>
    </div>

    <!-- Settings Modal -->
    <div v-if="showSettingsModal" class="modal-overlay" @click.self="closeSettings">
      <div class="modal settings-modal">
        <div class="modal-header">
          <h2>Theme Settings: {{ selectedTheme?.name }}</h2>
          <button class="close-btn" @click="closeSettings">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="loadingSettings" class="loading-container">
            <div class="loading-spinner"></div>
          </div>
          <div v-else-if="settingsSchema">
            <div 
              v-for="group in settingsSchema.settings" 
              :key="group.group" 
              class="settings-group"
            >
              <h3>{{ group.label }}</h3>
              <div 
                v-for="item in group.items" 
                :key="item.name" 
                class="setting-item"
              >
                <label :for="item.name">{{ item.label }}</label>
                <p v-if="item.description" class="setting-description">{{ item.description }}</p>
                
                <!-- Text Input -->
                <input 
                  v-if="item.type === 'text'" 
                  type="text" 
                  :id="item.name"
                  v-model="settingsValues[item.name]"
                  class="input"
                >
                
                <!-- Textarea -->
                <textarea 
                  v-else-if="item.type === 'textarea'" 
                  :id="item.name"
                  v-model="(settingsValues[item.name] as string)"
                  class="input textarea"
                  rows="3"
                ></textarea>
                
                <!-- Switch -->
                <label v-else-if="item.type === 'switch'" class="switch">
                  <input 
                    type="checkbox" 
                    :id="item.name"
                    v-model="settingsValues[item.name]"
                  >
                  <span class="slider"></span>
                </label>
                
                <!-- Select -->
                <select 
                  v-else-if="item.type === 'select'" 
                  :id="item.name"
                  v-model="settingsValues[item.name]"
                  class="input"
                >
                  <option 
                    v-for="option in item.options" 
                    :key="option.value" 
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
                
                <!-- Color -->
                <input 
                  v-else-if="item.type === 'color'" 
                  type="color" 
                  :id="item.name"
                  v-model="settingsValues[item.name]"
                  class="color-input"
                >
                
                <!-- Number -->
                <input 
                  v-else-if="item.type === 'number'" 
                  type="number" 
                  :id="item.name"
                  v-model="settingsValues[item.name]"
                  class="input"
                >
              </div>
            </div>
          </div>
          <p v-else class="no-settings">No settings available for this theme.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeSettings">Cancel</button>
          <button class="btn btn-primary" @click="saveSettings" :disabled="savingSettings">
            {{ savingSettings ? 'Saving...' : 'Save Settings' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
      <div class="modal delete-modal">
        <div class="modal-header">
          <h2>Confirm Delete</h2>
          <button class="close-btn" @click="closeDeleteModal">&times;</button>
        </div>
        <div class="modal-body">
          <p>Are you sure you want to delete the theme <strong>{{ themeToDelete?.name }}</strong>?</p>
          <p class="warning">This action cannot be undone.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeDeleteModal">Cancel</button>
          <button class="btn btn-danger" @click="deleteTheme" :disabled="deleting">
            {{ deleting ? 'Deleting...' : 'Delete Theme' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Preview Modal -->
    <div v-if="showPreviewModal" class="modal-overlay preview-modal-overlay" @click.self="closePreview">
      <div class="modal preview-modal">
        <div class="modal-header">
          <h2>Preview: {{ previewThemeId }}</h2>
          <button class="close-btn" @click="closePreview">&times;</button>
        </div>
        <div class="modal-body preview-body">
          <iframe :src="previewUrl" class="preview-iframe"></iframe>
        </div>
      </div>
    </div>

    <!-- Toast Messages -->
    <div v-if="toastMessage" class="toast" :class="toastType">
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import themeApi, { type Theme, type ThemeConfig } from '@/api/themes'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const themes = ref<Theme[]>([])
const loading = ref(true)
const activating = ref(false)

// Settings Modal
const showSettingsModal = ref(false)
const selectedTheme = ref<Theme | null>(null)
const settingsSchema = ref<ThemeConfig | null>(null)
const settingsValues = ref<Record<string, unknown>>({})
const loadingSettings = ref(false)
const savingSettings = ref(false)

// Delete Modal
const showDeleteModal = ref(false)
const themeToDelete = ref<Theme | null>(null)
const deleting = ref(false)

// Preview Modal
const showPreviewModal = ref(false)
const previewThemeId = ref('')
const previewUrl = ref('')

// Toast
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('success')

const loadThemes = async () => {
  loading.value = true
  try {
    const response = await themeApi.list()
    themes.value = response.data
  } catch (error) {
    showToast('Failed to load themes', 'error')
    console.error('Failed to load themes:', error)
  } finally {
    loading.value = false
  }
}

const getScreenshotUrl = (themeId: string) => {
  return themeApi.getScreenshotUrl(themeId)
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

const activateTheme = async (themeId: string) => {
  activating.value = true
  try {
    await themeApi.activate(themeId)
    await loadThemes()
    // Update theme store so App.vue applies the new theme
    await themeStore.fetchActiveTheme()
    showToast('Theme activated successfully', 'success')
  } catch (error) {
    showToast('Failed to activate theme', 'error')
    console.error('Failed to activate theme:', error)
  } finally {
    activating.value = false
  }
}

const enableTheme = async (themeId: string) => {
  try {
    await themeApi.enable(themeId)
    await loadThemes()
    showToast('Theme enabled successfully', 'success')
  } catch (error) {
    showToast('Failed to enable theme', 'error')
    console.error('Failed to enable theme:', error)
  }
}

const disableTheme = async (themeId: string) => {
  try {
    await themeApi.disable(themeId)
    await loadThemes()
    showToast('Theme disabled successfully', 'success')
  } catch (error) {
    showToast('Failed to disable theme', 'error')
    console.error('Failed to disable theme:', error)
  }
}

const openSettings = async (theme: Theme) => {
  selectedTheme.value = theme
  showSettingsModal.value = true
  loadingSettings.value = true
  
  try {
    const response = await themeApi.getSettingsSchema(theme.themeId)
    settingsSchema.value = response.data
    
    // Initialize settings values with current settings or defaults
    settingsValues.value = {}
    if (settingsSchema.value?.settings) {
      for (const group of settingsSchema.value.settings) {
        for (const item of group.items) {
          settingsValues.value[item.name] = 
            theme.settings?.[item.name] ?? item.defaultValue ?? ''
        }
      }
    }
  } catch (error) {
    showToast('Failed to load theme settings', 'error')
    console.error('Failed to load settings:', error)
  } finally {
    loadingSettings.value = false
  }
}

const closeSettings = () => {
  showSettingsModal.value = false
  selectedTheme.value = null
  settingsSchema.value = null
  settingsValues.value = {}
}

const saveSettings = async () => {
  if (!selectedTheme.value) return
  
  savingSettings.value = true
  try {
    await themeApi.updateSettings(selectedTheme.value.themeId, settingsValues.value)
    await loadThemes()
    // Update theme store so App.vue applies the new settings (like background)
    await themeStore.fetchActiveTheme()
    showToast('Settings saved successfully', 'success')
    closeSettings()
  } catch (error) {
    showToast('Failed to save settings', 'error')
    console.error('Failed to save settings:', error)
  } finally {
    savingSettings.value = false
  }
}

const previewTheme = (themeId: string) => {
  previewThemeId.value = themeId
  previewUrl.value = themeApi.getPreviewUrl(themeId)
  showPreviewModal.value = true
}

const closePreview = () => {
  showPreviewModal.value = false
  previewThemeId.value = ''
  previewUrl.value = ''
}

const confirmDelete = (theme: Theme) => {
  themeToDelete.value = theme
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  themeToDelete.value = null
}

const deleteTheme = async () => {
  if (!themeToDelete.value) return
  
  deleting.value = true
  try {
    await themeApi.delete(themeToDelete.value.themeId)
    await loadThemes()
    showToast('Theme deleted successfully', 'success')
    closeDeleteModal()
  } catch (error) {
    showToast('Failed to delete theme', 'error')
    console.error('Failed to delete theme:', error)
  } finally {
    deleting.value = false
  }
}

const handleUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  try {
    await themeApi.install(file)
    await loadThemes()
    showToast('Theme installed successfully', 'success')
  } catch (error) {
    showToast('Failed to install theme', 'error')
    console.error('Failed to install theme:', error)
  }
  
  // Reset input
  input.value = ''
}

const showToast = (message: string, type: 'success' | 'error') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(() => {
    toastMessage.value = ''
  }, 3000)
}

onMounted(() => {
  loadThemes()
})
</script>

<style scoped>
.theme-manager {
  padding: 20px;
  font-family: 'Noto Sans SC', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'WenQuanYi Micro Hei', 'Heiti SC', -apple-system, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 1.75rem;
  color: var(--text-primary);
}

.upload-btn {
  cursor: pointer;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-secondary);
}

.themes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.theme-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all var(--transition-medium);
  border: 2px solid transparent;
}

.theme-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--glow-primary);
}

.theme-card.active {
  border-color: var(--color-primary);
}

.theme-card.disabled {
  opacity: 0.6;
}

.theme-screenshot {
  position: relative;
  height: 200px;
  background: var(--bg-secondary);
  overflow: hidden;
}

.theme-screenshot img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-screenshot {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 4rem;
}

.theme-status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 6px 12px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
}

.theme-status-badge.disabled {
  background: var(--text-muted);
}

.theme-info {
  padding: 20px;
}

.theme-info h3 {
  margin-bottom: 4px;
  color: var(--text-primary);
}

.theme-version {
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.theme-author {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.theme-author a {
  color: var(--color-primary);
}

.theme-description {
  font-size: 0.875rem;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.theme-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 20px 20px;
}

.btn {
  padding: 8px 16px;
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--color-primary);
  color: white;
}

.btn-outline {
  background: transparent;
  border: 1px solid var(--text-muted);
  color: var(--text-secondary);
}

.btn-outline:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}

/* Modal Styles */
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

.modal {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--bg-secondary);
}

.modal-header h2 {
  font-size: 1.25rem;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
}

.close-btn:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid var(--bg-secondary);
}

/* Settings Modal */
.settings-group {
  margin-bottom: 24px;
}

.settings-group h3 {
  font-size: 1rem;
  color: var(--color-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--bg-secondary);
}

.setting-item {
  margin-bottom: 16px;
}

.setting-item label {
  display: block;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.setting-description {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.input {
  width: 100%;
  padding: 10px 14px;
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 0.875rem;
}

.input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.textarea {
  resize: vertical;
  min-height: 80px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-secondary);
  transition: 0.3s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .slider {
  background: var(--color-primary);
}

input:checked + .slider:before {
  transform: translateX(24px);
}

.color-input {
  width: 60px;
  height: 40px;
  padding: 4px;
  background: var(--bg-secondary);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.no-settings {
  text-align: center;
  color: var(--text-muted);
  padding: 40px;
}

/* Delete Modal */
.delete-modal .warning {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 8px;
}

/* Preview Modal */
.preview-modal-overlay {
  padding: 40px;
}

.preview-modal {
  max-width: 1200px;
  width: 100%;
  height: 90vh;
}

.preview-body {
  padding: 0;
  flex: 1;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: white;
}

/* Toast */
.toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 12px 24px;
  border-radius: var(--radius-md);
  color: white;
  font-weight: 500;
  animation: slideIn 0.3s ease;
  z-index: 2000;
}

.toast.success {
  background: #10b981;
}

.toast.error {
  background: #ef4444;
}

@keyframes slideIn {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .themes-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>
