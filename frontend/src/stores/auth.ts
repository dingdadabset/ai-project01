import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import api from '@/api/index'

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const isLoading = ref(false)

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  // Actions
  async function login(username: string, password: string) {
    isLoading.value = true
    try {
      const response = await api.post<{ token: string; user: User }>('/auth/login', {
        username,
        password
      })
      token.value = response.data.token
      user.value = response.data.user
      localStorage.setItem('token', response.data.token)
      return { success: true }
    } catch (error: any) {
      console.error('Login failed:', error)
      return { success: false, error: error.response?.data?.message || 'Login failed' }
    } finally {
      isLoading.value = false
    }
  }

  async function register(data: {
    username: string
    password: string
    email: string
    nickname?: string
  }) {
    isLoading.value = true
    try {
      const response = await api.post<{ token: string; user: User }>('/auth/register', data)
      token.value = response.data.token
      user.value = response.data.user
      localStorage.setItem('token', response.data.token)
      return { success: true }
    } catch (error: any) {
      console.error('Registration failed:', error)
      return { success: false, error: error.response?.data?.message || 'Registration failed' }
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    isLoading.value = true
    try {
      const response = await api.get<User>('/auth/me')
      user.value = response.data
    } catch (error) {
      console.error('Failed to fetch user:', error)
      logout()
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  // Initialize function to be called after app setup
  async function initialize() {
    if (token.value) {
      await fetchCurrentUser()
    }
  }

  return {
    // State
    user,
    token,
    isLoading,
    // Getters
    isAuthenticated,
    isAdmin,
    // Actions
    login,
    register,
    fetchCurrentUser,
    logout,
    initialize
  }
})
