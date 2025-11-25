import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Post, Category, Tag, ViewMode } from '@/types'
import postApi from '@/api/posts'
import categoryApi from '@/api/categories'
import tagApi from '@/api/tags'

export const useAppStore = defineStore('app', () => {
  // State
  const viewMode = ref<ViewMode>('story')
  const isLoading = ref(false)
  const isDarkMode = ref(false)
  const sidebarOpen = ref(false)
  
  // Posts
  const posts = ref<Post[]>([])
  const currentPost = ref<Post | null>(null)
  const totalPosts = ref(0)
  const currentPage = ref(0)
  
  // Categories
  const categories = ref<Category[]>([])
  
  // Tags
  const tags = ref<Tag[]>([])

  // Getters
  const featuredPosts = computed(() => 
    posts.value.filter(p => p.status === 'PUBLISHED').slice(0, 3)
  )
  
  const recentPosts = computed(() =>
    posts.value.filter(p => p.status === 'PUBLISHED').slice(0, 5)
  )

  // Actions
  async function fetchPosts(page = 0, size = 10) {
    isLoading.value = true
    try {
      const response = await postApi.listPublished(page, size)
      posts.value = response.data.records || []
      totalPosts.value = response.data.total || 0
      currentPage.value = page
    } catch (error) {
      console.error('Failed to fetch posts:', error)
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPost(slug: string) {
    isLoading.value = true
    try {
      const response = await postApi.getBySlug(slug)
      currentPost.value = response.data
    } catch (error) {
      console.error('Failed to fetch post:', error)
      currentPost.value = null
    } finally {
      isLoading.value = false
    }
  }

  async function fetchCategories() {
    try {
      const response = await categoryApi.list()
      categories.value = response.data || []
    } catch (error) {
      console.error('Failed to fetch categories:', error)
    }
  }

  async function fetchTags() {
    try {
      const response = await tagApi.list()
      tags.value = response.data || []
    } catch (error) {
      console.error('Failed to fetch tags:', error)
    }
  }

  function toggleViewMode() {
    viewMode.value = viewMode.value === 'story' ? 'list' : 'story'
  }

  function toggleDarkMode() {
    isDarkMode.value = !isDarkMode.value
    document.documentElement.classList.toggle('dark', isDarkMode.value)
  }

  function toggleSidebar() {
    sidebarOpen.value = !sidebarOpen.value
  }

  return {
    // State
    viewMode,
    isLoading,
    isDarkMode,
    sidebarOpen,
    posts,
    currentPost,
    totalPosts,
    currentPage,
    categories,
    tags,
    // Getters
    featuredPosts,
    recentPosts,
    // Actions
    fetchPosts,
    fetchPost,
    fetchCategories,
    fetchTags,
    toggleViewMode,
    toggleDarkMode,
    toggleSidebar
  }
})
