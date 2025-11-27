import api from './index'

export interface Theme {
  id: number
  themeId: string
  name: string
  version: string
  author: string
  authorUrl?: string
  description?: string
  screenshot?: string
  isActive: boolean
  status: 'ENABLED' | 'DISABLED' | 'ERROR'
  config?: ThemeConfig
  settings?: Record<string, unknown>
  createdAt: string
  updatedAt: string
}

export interface ThemeConfig {
  id: string
  name: string
  version: string
  author?: {
    name: string
    website?: string
    email?: string
  }
  description?: string
  screenshot?: string
  requires?: string
  website?: string
  repo?: string
  settings?: SettingGroup[]
  i18n?: {
    defaultLocale: string
    supportedLocales: string[]
  }
  features?: {
    darkMode: boolean
    responsive: boolean
    pwa: boolean
    comments: boolean
    search: boolean
  }
}

export interface SettingGroup {
  group: string
  label: string
  items: SettingItem[]
}

export interface SettingItem {
  name: string
  label: string
  type: 'text' | 'textarea' | 'switch' | 'select' | 'color' | 'number'
  description?: string
  defaultValue?: unknown
  options?: { label: string; value: string }[]
  validation?: Record<string, unknown>
}

export interface ThemeRequest {
  settings: Record<string, unknown>
}

const themeApi = {
  // Get all themes
  list() {
    return api.get<Theme[]>('/themes')
  },

  // Get theme by ID
  getById(id: number) {
    return api.get<Theme>(`/themes/${id}`)
  },

  // Get theme by theme ID
  getByThemeId(themeId: string) {
    return api.get<Theme>(`/themes/by-theme-id/${themeId}`)
  },

  // Get active theme
  getActive() {
    return api.get<Theme>('/themes/active')
  },

  // Activate theme
  activate(themeId: string) {
    return api.post<Theme>(`/themes/${themeId}/activate`)
  },

  // Enable theme
  enable(themeId: string) {
    return api.post<Theme>(`/themes/${themeId}/enable`)
  },

  // Disable theme
  disable(themeId: string) {
    return api.post<Theme>(`/themes/${themeId}/disable`)
  },

  // Update theme settings
  updateSettings(themeId: string, settings: Record<string, unknown>) {
    return api.put<Theme>(`/themes/${themeId}/settings`, { settings })
  },

  // Get theme settings schema
  getSettingsSchema(themeId: string) {
    return api.get<ThemeConfig>(`/themes/${themeId}/settings/schema`)
  },

  // Install theme from file
  install(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<Theme>('/themes/install', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Delete theme
  delete(themeId: string) {
    return api.delete(`/themes/${themeId}`)
  },

  // Get theme screenshot URL
  getScreenshotUrl(themeId: string) {
    return `/api/themes/${themeId}/screenshot`
  },

  // Get theme preview URL
  getPreviewUrl(themeId: string) {
    return `/themes/${themeId}/preview`
  }
}

export default themeApi
