<script setup lang="ts">
import { onMounted, computed, watch, ref } from 'vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import NavBar from '@/components/layout/NavBar.vue'
import FooterBar from '@/components/layout/FooterBar.vue'
import { useThemeStore } from '@/stores/theme'
import gsap from 'gsap'

const router = useRouter()
const route = useRoute()
const themeStore = useThemeStore()

// Current background class
const currentBackgroundClass = ref('')

// Check if current route is admin or login
const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin') || route.path === '/login'
})

// Background style based on theme settings
const backgroundStyle = computed(() => {
  if (!themeStore.activeTheme?.settings) return {}
  
  const settings = themeStore.activeTheme.settings
  const themeId = themeStore.activeTheme.themeId
  
  // Only apply background for anime-girls theme
  if (themeId !== 'anime-girls') return {}
  
  const currentBackground = settings.currentBackground as string || 'bg1'
  const customBackgroundUrl = settings.customBackgroundUrl as string
  const backgroundOpacity = settings.backgroundOpacity as string || '0.85'
  
  // If custom URL is provided, use it
  if (customBackgroundUrl) {
    return {
      '--theme-background-image': `url(${customBackgroundUrl})`,
      '--theme-background-opacity': backgroundOpacity
    }
  }
  
  // Use predefined background
  const backgroundUrl = `/themes/anime-girls/static/images/backgrounds/${currentBackground}.svg`
  return {
    '--theme-background-image': `url(${backgroundUrl})`,
    '--theme-background-opacity': backgroundOpacity
  }
})

// Color settings from theme
const themeColors = computed(() => {
  if (!themeStore.activeTheme?.settings) return {}
  
  const settings = themeStore.activeTheme.settings
  const themeId = themeStore.activeTheme.themeId
  
  if (themeId !== 'anime-girls') return {}
  
  return {
    '--theme-primary-color': settings.primaryColor as string || '#ff69b4',
    '--theme-accent-color': settings.accentColor as string || '#ff1493',
    '--theme-text-color': settings.textColor as string || '#ffffff'
  }
})

// Combined theme styles
const themeStyles = computed(() => {
  return {
    ...backgroundStyle.value,
    ...themeColors.value
  }
})

// Is anime-girls theme active
const isAnimeTheme = computed(() => {
  return themeStore.activeTheme?.themeId === 'anime-girls'
})

// Watch for theme changes and apply background
watch(() => themeStore.activeTheme, (newTheme) => {
  if (newTheme?.themeId === 'anime-girls') {
    const bg = newTheme.settings?.currentBackground as string || 'bg1'
    currentBackgroundClass.value = `background-${bg}`
    applyThemeStyles()
  } else {
    currentBackgroundClass.value = ''
    removeThemeStyles()
  }
}, { immediate: true, deep: true })

// Apply theme styles to document
function applyThemeStyles() {
  const root = document.documentElement
  const settings = themeStore.activeTheme?.settings
  
  if (settings) {
    // Apply colors
    if (settings.primaryColor) {
      root.style.setProperty('--color-primary', settings.primaryColor as string)
      root.style.setProperty('--color-primary-dark', settings.accentColor as string || settings.primaryColor as string)
    }
    if (settings.textColor) {
      root.style.setProperty('--text-primary', settings.textColor as string)
    }
  }
}

function removeThemeStyles() {
  const root = document.documentElement
  root.style.removeProperty('--color-primary')
  root.style.removeProperty('--color-primary-dark')
  root.style.removeProperty('--text-primary')
}

// Page transition animation
router.beforeEach((to, from, next) => {
  const overlay = document.querySelector('.page-transition-overlay')
  if (overlay && from.name) {
    gsap.to(overlay, {
      scaleY: 1,
      transformOrigin: 'bottom',
      duration: 0.4,
      ease: 'power4.inOut',
      onComplete: () => {
        next()
        setTimeout(() => {
          gsap.to(overlay, {
            scaleY: 0,
            transformOrigin: 'top',
            duration: 0.4,
            ease: 'power4.inOut'
          })
        }, 100)
      }
    })
  } else {
    next()
  }
})

onMounted(async () => {
  // Fetch active theme to apply settings
  await themeStore.fetchActiveTheme()
  
  // Initial page load animation
  gsap.fromTo(
    '.main-content',
    { opacity: 0 },
    { opacity: 1, duration: 0.5, ease: 'power2.out' }
  )
})
</script>

<template>
  <div id="app" :class="[currentBackgroundClass, { 'anime-theme': isAnimeTheme }]" :style="themeStyles">
    <!-- Theme Background Layer -->
    <div v-if="isAnimeTheme" class="theme-background-layer"></div>
    
    <!-- Page Transition Overlay -->
    <div class="page-transition-overlay"></div>
    
    <!-- Navigation (hide on admin/login routes) -->
    <NavBar v-if="!isAdminRoute" />
    
    <!-- Main Content -->
    <main class="main-content" :class="{ 'no-padding': isAdminRoute }">
      <RouterView v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    
    <!-- Footer (hide on admin/login routes) -->
    <FooterBar v-if="!isAdminRoute" />
  </div>
</template>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Theme Background Layer for anime-girls theme */
.theme-background-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -2;
  background-image: var(--theme-background-image);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.theme-background-layer::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, calc(1 - var(--theme-background-opacity, 0.85)));
  pointer-events: none;
}

/* Anime theme specific styles */
#app.anime-theme {
  --color-primary: var(--theme-primary-color, #ff69b4);
  --color-primary-dark: var(--theme-accent-color, #ff1493);
}

/* Background classes for different anime backgrounds */
#app.background-bg1 .theme-background-layer {
  background: 
    linear-gradient(135deg, rgba(255, 182, 193, 0.3) 0%, transparent 50%),
    linear-gradient(225deg, rgba(255, 105, 180, 0.3) 0%, transparent 50%),
    linear-gradient(45deg, #1a0a1a 0%, #2d1f3d 50%, #1a0a1a 100%);
}

#app.background-bg2 .theme-background-layer {
  background: 
    radial-gradient(ellipse at top, #0c1445 0%, #020510 50%),
    linear-gradient(to bottom, #020510 0%, #0a0a2e 100%);
}

#app.background-bg3 .theme-background-layer {
  background: 
    linear-gradient(180deg, 
      #ff7e5f 0%, 
      #feb47b 25%, 
      #ffb88c 40%,
      #de6262 60%,
      #2c3e50 80%,
      #1a252f 100%);
}

#app.background-bg4 .theme-background-layer {
  background: 
    linear-gradient(135deg, #134e5e 0%, #71b280 100%);
}

#app.background-bg5 .theme-background-layer {
  background: 
    linear-gradient(180deg, 
      #0f0c29 0%, 
      #302b63 50%, 
      #24243e 100%);
}

.main-content {
  flex: 1;
  padding-top: 80px;
  position: relative;
  z-index: 1;
}

.main-content.no-padding {
  padding-top: 0;
}

/* Page transition overlay */
.page-transition-overlay {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  transform: scaleY(0);
  transform-origin: bottom;
  z-index: 9999;
  pointer-events: none;
}

/* Vue transition classes */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
