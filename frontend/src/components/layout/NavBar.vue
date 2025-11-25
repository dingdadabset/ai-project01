<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import gsap from 'gsap'

const store = useAppStore()
const route = useRoute()
const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)
const navRef = ref<HTMLElement | null>(null)

const navLinks = [
  { name: 'Home', path: '/' },
  { name: 'Posts', path: '/posts' },
  { name: 'Categories', path: '/categories' },
  { name: 'Tags', path: '/tags' },
  { name: 'About', path: '/about' }
]

// Close mobile menu on route change
watch(() => route.path, () => {
  isMobileMenuOpen.value = false
})

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

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

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
      
      <!-- Nav Links (Desktop) -->
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
      
      <!-- Actions (Desktop) -->
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
      <button 
        class="mobile-menu-toggle" 
        :class="{ 'active': isMobileMenuOpen }"
        @click="toggleMobileMenu"
        aria-label="Toggle menu"
      >
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
        <span class="menu-bar"></span>
      </button>
    </div>
    
    <!-- Mobile Menu -->
    <div class="mobile-menu" :class="{ 'open': isMobileMenuOpen }">
      <div class="mobile-menu-content">
        <!-- Mobile Nav Links -->
        <div class="mobile-nav-links">
          <RouterLink 
            v-for="link in navLinks" 
            :key="link.path"
            :to="link.path"
            class="mobile-nav-link"
            @click="isMobileMenuOpen = false"
          >
            {{ link.name }}
          </RouterLink>
        </div>
        
        <!-- Mobile Actions -->
        <div class="mobile-actions">
          <!-- View Mode Toggle -->
          <div class="mobile-view-toggle">
            <span class="toggle-label">View Mode</span>
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
          </div>
          
          <!-- Dark Mode Toggle -->
          <div class="mobile-theme-toggle">
            <span class="toggle-label">Theme</span>
            <button class="theme-toggle" @click="store.toggleDarkMode()">
              {{ store.isDarkMode ? '🌞 Light' : '🌙 Dark' }}
            </button>
          </div>
          
          <!-- Admin Link -->
          <RouterLink 
            to="/admin" 
            class="btn btn-primary mobile-admin-btn"
            @click="isMobileMenuOpen = false"
          >
            Admin Panel
          </RouterLink>
        </div>
      </div>
    </div>
    
    <!-- Mobile Menu Overlay -->
    <div 
      v-if="isMobileMenuOpen" 
      class="mobile-menu-overlay"
      @click="isMobileMenuOpen = false"
    ></div>
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

/* Mobile Menu Toggle */
.mobile-menu-toggle {
  display: none;
  flex-direction: column;
  gap: 5px;
  padding: 10px;
  background: none;
  border: none;
  cursor: pointer;
  z-index: 1002;
}

.menu-bar {
  width: 24px;
  height: 2px;
  background: var(--text-primary);
  transition: all 0.3s ease;
}

.mobile-menu-toggle.active .menu-bar:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.mobile-menu-toggle.active .menu-bar:nth-child(2) {
  opacity: 0;
}

.mobile-menu-toggle.active .menu-bar:nth-child(3) {
  transform: rotate(-45deg) translate(5px, -5px);
}

/* Mobile Menu */
.mobile-menu {
  display: none;
  position: fixed;
  top: 0;
  right: -100%;
  width: 80%;
  max-width: 320px;
  height: 100vh;
  background: var(--bg-secondary);
  z-index: 1001;
  transition: right 0.3s ease;
  overflow-y: auto;
}

.mobile-menu.open {
  right: 0;
}

.mobile-menu-content {
  padding: 100px 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.mobile-nav-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-link {
  display: block;
  padding: 16px 20px;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 1.1rem;
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.mobile-nav-link:hover,
.mobile-nav-link.router-link-active {
  background: rgba(99, 102, 241, 0.1);
  color: var(--color-primary);
}

.mobile-actions {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.mobile-view-toggle,
.mobile-theme-toggle {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toggle-label {
  font-size: 0.875rem;
  color: var(--text-muted);
  font-weight: 500;
}

.mobile-theme-toggle .theme-toggle {
  width: auto;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  font-size: 1rem;
}

.mobile-admin-btn {
  width: 100%;
  text-align: center;
  padding: 14px 24px;
}

/* Mobile Menu Overlay */
.mobile-menu-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

@media (max-width: 768px) {
  .nav-links,
  .nav-actions {
    display: none;
  }
  
  .mobile-menu-toggle {
    display: flex;
  }
  
  .mobile-menu {
    display: block;
  }
  
  .mobile-menu-overlay {
    display: block;
  }
}

@media (max-width: 480px) {
  .nav-logo .logo-text {
    font-size: 1.2rem;
  }
  
  .mobile-menu {
    width: 100%;
    max-width: none;
  }
}
</style>
