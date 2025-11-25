<script setup lang="ts">
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const navItems = [
  { path: '/admin', name: 'Dashboard', icon: '📊' },
  { path: '/admin/posts', name: 'Posts', icon: '📝' },
  { path: '/admin/categories', name: 'Categories', icon: '📁' },
  { path: '/admin/tags', name: 'Tags', icon: '🏷️' },
  { path: '/admin/comments', name: 'Comments', icon: '💬' },
  { path: '/admin/users', name: 'Users', icon: '👥' }
]

const isActive = (path: string) => {
  if (path === '/admin') {
    return route.path === path
  }
  return route.path.startsWith(path)
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <RouterLink to="/" class="brand">
          <span class="brand-icon">✨</span>
          <span class="brand-text">Halo Admin</span>
        </RouterLink>
      </div>
      
      <nav class="sidebar-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'active': isActive(item.path) }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text">{{ item.name }}</span>
        </RouterLink>
      </nav>
      
      <div class="sidebar-footer">
        <div v-if="authStore.user" class="user-info">
          <span class="user-avatar">{{ authStore.user.username.charAt(0).toUpperCase() }}</span>
          <span class="user-name">{{ authStore.user.nickname || authStore.user.username }}</span>
        </div>
        <div class="footer-actions">
          <RouterLink to="/" class="back-to-site">
            ← Back to Site
          </RouterLink>
          <button v-if="authStore.isAuthenticated" class="logout-btn" @click="handleLogout">
            Logout
          </button>
          <RouterLink v-else to="/login" class="login-link">
            Login
          </RouterLink>
        </div>
      </div>
    </aside>
    
    <!-- Main Content -->
    <main class="admin-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 260px;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  padding: 24px;
}

.sidebar-header {
  margin-bottom: 32px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: inherit;
}

.brand-icon {
  font-size: 1.5rem;
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: rgba(99, 102, 241, 0.1);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--color-primary);
  color: white;
}

.nav-icon {
  font-size: 1.25rem;
}

.sidebar-footer {
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-md);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.user-name {
  font-weight: 500;
  font-size: 0.9rem;
}

.footer-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.back-to-site {
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

.back-to-site:hover {
  color: var(--color-primary);
}

.logout-btn {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

.login-link {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.875rem;
  padding: 8px 16px;
  background: rgba(99, 102, 241, 0.1);
  border-radius: var(--radius-md);
  text-align: center;
  transition: all 0.3s ease;
}

.login-link:hover {
  background: rgba(99, 102, 241, 0.2);
}

.admin-main {
  flex: 1;
  margin-left: 260px;
  padding: 32px;
}
</style>
