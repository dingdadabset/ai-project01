import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Theme } from '@/types'
import themeApi from '@/api/themes'

export const useThemeStore = defineStore('theme', () => {
  // State
  const themes = ref<Theme[]>([])
  const activeTheme = ref<Theme | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const enabledThemes = computed(() => 
    themes.value.filter(t => t.status === 'ENABLED')
  )

  const hasActiveTheme = computed(() => activeTheme.value !== null)

  // Actions
  async function fetchThemes() {
    isLoading.value = true
    error.value = null
    try {
      const response = await themeApi.list()
      themes.value = response.data
      activeTheme.value = themes.value.find(t => t.isActive) || null
    } catch (e) {
      error.value = 'Failed to fetch themes'
      console.error('Failed to fetch themes:', e)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchActiveTheme() {
    try {
      const response = await themeApi.getActive()
      activeTheme.value = response.data
    } catch (e) {
      console.error('Failed to fetch active theme:', e)
    }
  }

  async function activateTheme(themeId: string) {
    isLoading.value = true
    try {
      const response = await themeApi.activate(themeId)
      activeTheme.value = response.data
      await fetchThemes()
      return true
    } catch (e) {
      error.value = 'Failed to activate theme'
      console.error('Failed to activate theme:', e)
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function updateThemeSettings(themeId: string, settings: Record<string, unknown>) {
    try {
      await themeApi.updateSettings(themeId, settings)
      await fetchThemes()
      return true
    } catch (e) {
      error.value = 'Failed to update theme settings'
      console.error('Failed to update settings:', e)
      return false
    }
  }

  function getThemeSettings(): Record<string, unknown> {
    return activeTheme.value?.settings || {}
  }

  function getThemeSetting<T>(key: string, defaultValue: T): T {
    const settings = getThemeSettings()
    return (settings[key] as T) ?? defaultValue
  }

  return {
    // State
    themes,
    activeTheme,
    isLoading,
    error,
    // Getters
    enabledThemes,
    hasActiveTheme,
    // Actions
    fetchThemes,
    fetchActiveTheme,
    activateTheme,
    updateThemeSettings,
    getThemeSettings,
    getThemeSetting
  }
})
