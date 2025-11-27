<template>
  <div class="background-selector-wrapper">
    <!-- Floating Button -->
    <button 
      class="floating-btn"
      @click="toggleSelector"
      :class="{ active: isOpen }"
    >
      <span class="btn-icon">🖼️</span>
      <span class="btn-tooltip">Change Background</span>
    </button>

    <!-- Selector Panel -->
    <transition name="slide-up">
      <div v-if="isOpen" class="selector-panel glass">
        <div class="panel-header">
          <h3>🎨 Background Selector</h3>
          <button class="close-btn" @click="toggleSelector">&times;</button>
        </div>

        <div class="panel-content">
          <!-- Custom Upload Section -->
          <div class="upload-section">
            <label class="upload-btn">
              <input 
                type="file" 
                accept="image/*" 
                @change="handleUpload" 
                hidden
              >
              <span class="upload-icon">📤</span>
              <span>Upload Custom Background</span>
            </label>
            <p class="upload-hint">Supports JPG, PNG, GIF, WebP, SVG</p>
          </div>

          <!-- Custom URL Section -->
          <div class="url-section">
            <input 
              type="text" 
              v-model="customUrl" 
              placeholder="Or paste image URL here..."
              class="url-input"
              @keyup.enter="applyCustomUrl"
            >
            <button 
              class="apply-btn" 
              @click="applyCustomUrl"
              :disabled="!customUrl"
            >
              Apply
            </button>
          </div>

          <!-- Preset Backgrounds -->
          <div class="preset-section">
            <h4>Preset Backgrounds (预设背景)</h4>
            <div class="preset-grid">
              <button
                v-for="bg in presetBackgrounds"
                :key="bg.value"
                class="preset-item"
                :class="{ active: currentBackground === bg.value }"
                @click="selectBackground(bg.value)"
              >
                <div 
                  class="preset-preview"
                  :style="{ background: bg.preview }"
                ></div>
                <span class="preset-label">{{ bg.label }}</span>
              </button>
            </div>
          </div>

          <!-- Blur Settings -->
          <div class="blur-section">
            <h4>Blur Effect (虚化效果)</h4>
            <div class="blur-options">
              <button
                v-for="blur in blurOptions"
                :key="blur.value"
                class="blur-option"
                :class="{ active: currentBlur === blur.value }"
                @click="setBlur(blur.value)"
              >
                {{ blur.label }}
              </button>
            </div>
          </div>

          <!-- Overlay Toggle -->
          <div class="overlay-section">
            <label class="overlay-toggle">
              <input 
                type="checkbox" 
                v-model="overlayEnabled"
                @change="toggleOverlay"
              >
              <span class="toggle-slider"></span>
              <span class="toggle-label">Show Background Overlay (背景覆盖层)</span>
            </label>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import uploadApi from '@/api/uploads'

const themeStore = useThemeStore()
const isOpen = ref(false)
const customUrl = ref('')
const uploading = ref(false)

// Preset backgrounds with gradient previews
const presetBackgrounds = [
  { value: 'bg1', label: '樱花少女', preview: 'linear-gradient(135deg, #ffb7c5, #ff69b4)' },
  { value: 'bg2', label: '星空幻想', preview: 'linear-gradient(135deg, #0c1445, #020510)' },
  { value: 'bg3', label: '日落海滩', preview: 'linear-gradient(135deg, #ff7e5f, #feb47b)' },
  { value: 'bg4', label: '魔法森林', preview: 'linear-gradient(135deg, #134e5e, #71b280)' },
  { value: 'bg5', label: '城市夜景', preview: 'linear-gradient(135deg, #0f0c29, #302b63)' },
  { value: 'bg6', label: '魔法少女', preview: 'linear-gradient(135deg, #667eea, #f093fb)' },
  { value: 'bg7', label: '翠绿仙子', preview: 'linear-gradient(135deg, #11998e, #38ef7d)' },
  { value: 'bg8', label: '月夜精灵', preview: 'linear-gradient(135deg, #0f0c29, #24243e)' },
  { value: 'bg9', label: '落日少女', preview: 'linear-gradient(135deg, #ff6b6b, #feca57)' },
  { value: 'bg10', label: '棉花糖天使', preview: 'linear-gradient(135deg, #a8edea, #fed6e3)' },
  { value: 'bg11', label: '赛博朋克', preview: 'linear-gradient(135deg, #1a1a2e, #0f3460)' },
  { value: 'bg12', label: '恋爱天使', preview: 'linear-gradient(135deg, #ee9ca7, #ffdde1)' },
  { value: 'bg13', label: '云端少女', preview: 'linear-gradient(135deg, #4facfe, #00f2fe)' },
  { value: 'bg14', label: '水晶公主', preview: 'linear-gradient(135deg, #8b5cf6, #d946ef)' },
  { value: 'bg15', label: '海洋精灵', preview: 'linear-gradient(135deg, #1e3c72, #2a5298)' },
  { value: 'bg16', label: '蜜桃少女', preview: 'linear-gradient(135deg, #ffecd2, #fcb69f)' },
  { value: 'bg17', label: '极光仙子', preview: 'linear-gradient(135deg, #0d0d0d, #2d2d2d)' },
  { value: 'bg18', label: '秋叶精灵', preview: 'linear-gradient(135deg, #2c1810, #daa520)' },
  { value: 'bg19', label: '森林仙女', preview: 'linear-gradient(135deg, #e8f5e9, #a5d6a7)' },
  { value: 'bg20', label: '梦幻少女', preview: 'linear-gradient(135deg, #ff9a9e, #ffd1ff)' },
]

const blurOptions = [
  { value: '0', label: 'None' },
  { value: '4', label: 'Light' },
  { value: '8', label: 'Medium' },
  { value: '12', label: 'Strong' },
  { value: '16', label: 'Very Strong' },
]

// Current values from theme settings
const currentBackground = computed(() => 
  themeStore.getThemeSetting('currentBackground', 'bg1') as string
)

const currentBlur = computed(() => 
  themeStore.getThemeSetting('backgroundBlur', '8') as string
)

const overlayEnabled = ref(true)

onMounted(() => {
  overlayEnabled.value = themeStore.getThemeSetting('backgroundOverlay', true) as boolean
  customUrl.value = themeStore.getThemeSetting('customBackgroundUrl', '') as string
})

const toggleSelector = () => {
  isOpen.value = !isOpen.value
}

const selectBackground = async (bgId: string) => {
  if (!themeStore.activeTheme) return
  
  const newSettings = {
    ...themeStore.activeTheme.settings,
    currentBackground: bgId,
    customBackgroundUrl: '' // Clear custom URL when selecting preset
  }
  
  await themeStore.updateThemeSettings(themeStore.activeTheme.themeId, newSettings)
  await themeStore.fetchActiveTheme()
  customUrl.value = ''
}

const handleUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  uploading.value = true
  try {
    const response = await uploadApi.upload(file, 'backgrounds')
    if (response.data.success && response.data.url) {
      await applyCustomBackground(response.data.url)
    }
  } catch (error) {
    console.error('Failed to upload background:', error)
  } finally {
    uploading.value = false
    input.value = ''
  }
}

const applyCustomUrl = async () => {
  if (!customUrl.value) return
  await applyCustomBackground(customUrl.value)
}

const applyCustomBackground = async (url: string) => {
  if (!themeStore.activeTheme) return
  
  const newSettings = {
    ...themeStore.activeTheme.settings,
    customBackgroundUrl: url
  }
  
  await themeStore.updateThemeSettings(themeStore.activeTheme.themeId, newSettings)
  await themeStore.fetchActiveTheme()
}

const setBlur = async (blurValue: string) => {
  if (!themeStore.activeTheme) return
  
  const newSettings = {
    ...themeStore.activeTheme.settings,
    backgroundBlur: blurValue
  }
  
  await themeStore.updateThemeSettings(themeStore.activeTheme.themeId, newSettings)
  await themeStore.fetchActiveTheme()
}

const toggleOverlay = async () => {
  if (!themeStore.activeTheme) return
  
  const newSettings = {
    ...themeStore.activeTheme.settings,
    backgroundOverlay: overlayEnabled.value
  }
  
  await themeStore.updateThemeSettings(themeStore.activeTheme.themeId, newSettings)
  await themeStore.fetchActiveTheme()
}
</script>

<style scoped>
.background-selector-wrapper {
  position: fixed;
  bottom: 100px;
  right: 24px;
  z-index: 1000;
}

.floating-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.4);
  transition: all 0.3s ease;
  position: relative;
}

.floating-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 30px rgba(99, 102, 241, 0.6);
}

.floating-btn.active {
  transform: rotate(45deg);
}

.btn-icon {
  font-size: 1.5rem;
}

.btn-tooltip {
  position: absolute;
  right: 70px;
  white-space: nowrap;
  background: var(--bg-card);
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: 0.875rem;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}

.floating-btn:hover .btn-tooltip {
  opacity: 1;
}

.selector-panel {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 360px;
  max-height: 70vh;
  overflow-y: auto;
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--bg-secondary);
}

.panel-header h3 {
  font-size: 1rem;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--text-muted);
}

.close-btn:hover {
  color: var(--text-primary);
}

.panel-content {
  padding: 16px 20px;
}

.upload-section {
  margin-bottom: 16px;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  background: var(--bg-secondary);
  border: 2px dashed var(--text-muted);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
  justify-content: center;
}

.upload-btn:hover {
  border-color: var(--color-primary);
  background: rgba(99, 102, 241, 0.1);
}

.upload-icon {
  font-size: 1.25rem;
}

.upload-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  text-align: center;
  margin-top: 8px;
}

.url-section {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.url-input {
  flex: 1;
  padding: 10px 12px;
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  font-size: 0.875rem;
}

.url-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.apply-btn {
  padding: 10px 16px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.apply-btn:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.apply-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.preset-section h4,
.blur-section h4 {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.preset-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.preset-item {
  background: none;
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  padding: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.preset-item:hover {
  border-color: var(--color-primary);
}

.preset-item.active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.3);
}

.preset-preview {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-xs);
}

.preset-label {
  display: block;
  font-size: 0.625rem;
  color: var(--text-secondary);
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.blur-section {
  margin-bottom: 16px;
}

.blur-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.blur-option {
  padding: 8px 12px;
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.blur-option:hover {
  border-color: var(--color-primary);
}

.blur-option.active {
  background: var(--color-primary);
  color: white;
}

.overlay-section {
  padding-top: 16px;
  border-top: 1px solid var(--bg-secondary);
}

.overlay-toggle {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.overlay-toggle input {
  display: none;
}

.toggle-slider {
  width: 44px;
  height: 24px;
  background: var(--bg-secondary);
  border-radius: 12px;
  position: relative;
  transition: all 0.3s ease;
}

.toggle-slider::after {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  background: white;
  border-radius: 50%;
  top: 3px;
  left: 3px;
  transition: all 0.3s ease;
}

.overlay-toggle input:checked + .toggle-slider {
  background: var(--color-primary);
}

.overlay-toggle input:checked + .toggle-slider::after {
  left: 23px;
}

.toggle-label {
  font-size: 0.875rem;
  color: var(--text-primary);
}

/* Transitions */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
