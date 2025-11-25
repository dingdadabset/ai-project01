<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAppStore } from '@/stores/app'
import gsap from 'gsap'

const store = useAppStore()
const isScrolled = ref(false)
const navRef = ref<HTMLElement | null>(null)

const navLinks = [
  { name: 'Home', path: '/' },
  { name: 'Posts', path: '/posts' },
  { name: 'Categories', path: '/categories' },
  { name: 'Tags', path: '/tags' },
  { name: 'About', path: '/about' }
]

onMounted(() => {
  // Animate nav on mount
  gsap.fromTo(
    navRef.value,
    { y: -100, opacity: 0 },
    { y: 0, opacity: 1, duration: 0.8, ease: 'power3.out' }
  )
  
  // Handle scroll
  window.addEventListener('scroll', () => {
    isScrolled.value = window.scrollY > 50
  })
})

const handleLogoHover = (entering: boolean) => {
  const logo = document.querySelector('.nav-logo')
  if (logo) {
    gsap.to(logo, {
      scale: entering ? 1.1 : 1,
      rotateZ: entering ? 5 : 0,
      duration: 0.3,
      ease: 'elastic.out(1, 0.5)'
    })
  }
}
</script>

<template>
  <nav 
    ref="navRef"
    class="navbar"
    :class="{ 'scrolled': isScrolled }"
  >
    <div class="nav-container">
      <!-- Logo -->
      <RouterLink 
        to="/" 
        class="nav-logo"
        @mouseenter="handleLogoHover(true)"
        @mouseleave="handleLogoHover(false)"
      >
        <span class="logo-icon">✨</span>
        <span class="logo-text">Halo Blog</span>
      </RouterLink>
      
      <!-- Nav Links -->
      <div class="nav-links">
        <RouterLink 
          v-for="link in navLinks" 
          :key="link.path"
          :to="link.path"
          class="nav-link"
        >
          {{ link.name }}
        </RouterLink>
      </div>
      
      <!-- Actions -->
      <div class="nav-actions">
        <!-- View Mode Toggle -->
        <div class="view-mode-toggle">
          <button 
            class="view-mode-btn"
            :class="{ 'active': store.viewMode === 'story' }"
            @click="store.toggleViewMode()"
          >
            🎬 Story
          </button>
          <button 
            class="view-mode-btn"
            :class="{ 'active': store.viewMode === 'list' }"
            @click="store.toggleViewMode()"
          >
            📋 List
          </button>
        </div>
        
        <!-- Dark Mode Toggle -->
        <button class="theme-toggle" @click="store.toggleDarkMode()">
          {{ store.isDarkMode ? '🌞' : '🌙' }}
        </button>
        
        <!-- Admin Link -->
        <RouterLink to="/admin" class="btn btn-primary btn-sm">
          Admin
        </RouterLink>
      </div>
      
      <!-- Mobile Menu Toggle -->
      <button class="mobile-menu-toggle" @click="store.toggleSidebar()">
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
      </button>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 20px 0;
  transition: all 0.3s ease;
}

.navbar.scrolled {
  background: rgba(15, 15, 35, 0.9);
  backdrop-filter: blur(20px);
  padding: 12px 0;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3);
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
}

.logo-icon {
  font-size: 1.8rem;
}

.logo-text {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-link {
  color: var(--text-secondary);
  font-weight: 500;
  padding: 8px 0;
  position: relative;
  transition: color 0.3s ease;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
  transition: width 0.3s ease;
}

.nav-link:hover {
  color: var(--text-primary);
}

.nav-link:hover::after {
  width: 100%;
}

.nav-link.router-link-active {
  color: var(--color-primary);
}

.nav-link.router-link-active::after {
  width: 100%;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-toggle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--bg-secondary);
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.theme-toggle:hover {
  transform: rotate(180deg);
}

.btn-sm {
  padding: 8px 16px;
  font-size: 0.75rem;
}

.mobile-menu-toggle {
  display: none;
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

@media (max-width: 768px) {
  .nav-links,
  .nav-actions {
    display: none;
  }
  
  .mobile-menu-toggle {
    display: flex;
  }
}
</style>
