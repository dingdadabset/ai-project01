<script setup lang="ts">
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const isSidebarOpen = ref(false)

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

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

const closeSidebar = () => {
  isSidebarOpen.value = false
}
</script>

<template>
  <div class="admin-layout">
    <!-- Mobile Header -->
    <header class="mobile-header">
      <button class="menu-toggle" @click="toggleSidebar" aria-label="Toggle menu">
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
      </button>
      <RouterLink to="/" class="brand">
        <span class="brand-icon">✨</span>
        <span class="brand-text">Halo Admin</span>
      </RouterLink>
    </header>
    
    <!-- Sidebar Overlay -->
    <div 
      v-if="isSidebarOpen" 
      class="sidebar-overlay"
      @click="closeSidebar"
    ></div>
    
    <!-- Sidebar -->
    <aside class="admin-sidebar" :class="{ 'open': isSidebarOpen }">
      <div class="sidebar-header">
        <RouterLink to="/" class="brand" @click="closeSidebar">
          <span class="brand-icon">✨</span>
          <span class="brand-text">Halo Admin</span>
        </RouterLink>
        <button class="close-sidebar" @click="closeSidebar" aria-label="Close menu">×</button>
      </div>
      
      <nav class="sidebar-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'active': isActive(item.path) }"
          @click="closeSidebar"
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
          <RouterLink to="/" class="back-to-site" @click="closeSidebar">
            ← Back to Site
          </RouterLink>
          <button v-if="authStore.isAuthenticated" class="logout-btn" @click="handleLogout">
            Logout
          </button>
          <RouterLink v-else to="/login" class="login-link" @click="closeSidebar">
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

/* Mobile Header */
.mobile-header {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--bg-secondary);
  z-index: 100;
  padding: 0 16px;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-toggle {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 10px;
  background: none;
  border: none;
  cursor: pointer;
}

.menu-bar {
  width: 24px;
  height: 2px;
  background: var(--text-primary);
  transition: all 0.3s ease;
}

/* Sidebar Overlay */
.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 199;
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
  z-index: 200;
  transition: transform 0.3s ease;
}

.sidebar-header {
  margin-bottom: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-sidebar {
  display: none;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
  padding: 5px;
}

.close-sidebar:hover {
  color: var(--text-primary);
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

/* Mobile Responsive */
@media (max-width: 768px) {
  .mobile-header {
    display: flex;
  }
  
  .sidebar-overlay {
    display: block;
  }
  
  .admin-sidebar {
    transform: translateX(-100%);
  }
  
  .admin-sidebar.open {
    transform: translateX(0);
  }
  
  .close-sidebar {
    display: block;
  }
  
  .admin-main {
    margin-left: 0;
    padding: 80px 16px 24px;
  }
}

@media (max-width: 480px) {
  .admin-sidebar {
    width: 100%;
  }
  
  .admin-main {
    padding: 70px 12px 20px;
  }
}
</style>
