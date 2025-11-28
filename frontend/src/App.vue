<script setup lang="ts">
import { onMounted, computed, watch, ref } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import NavBar from '@/components/layout/NavBar.vue'
import FooterBar from '@/components/layout/FooterBar.vue'
import BackgroundSelector from '@/components/common/BackgroundSelector.vue'
import { useThemeStore } from '@/stores/theme'

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
  const backgroundBlur = settings.backgroundBlur as string || '8'
  
  // If custom URL is provided, use it
  if (customBackgroundUrl) {
    return {
      '--theme-background-image': `url(${customBackgroundUrl})`,
      '--theme-background-opacity': backgroundOpacity,
      '--theme-background-blur': `${backgroundBlur}px`
    }
  }
  
  // Use predefined background
  const backgroundUrl = `/themes/anime-girls/static/images/backgrounds/${currentBackground}.svg`
  return {
    '--theme-background-image': `url(${backgroundUrl})`,
    '--theme-background-opacity': backgroundOpacity,
    '--theme-background-blur': `${backgroundBlur}px`
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

// Check if background overlay is enabled
const isOverlayEnabled = computed(() => {
  const settings = themeStore.activeTheme?.settings
  return settings?.backgroundOverlay !== false
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

// Page transition is handled by Vue's built-in transition component with fade effect

onMounted(async () => {
  // Fetch active theme to apply settings
  await themeStore.fetchActiveTheme()
})
</script>

<template>
  <div id="app" :class="[currentBackgroundClass, { 'anime-theme': isAnimeTheme }]" :style="themeStyles">
    <!-- Theme Background Layer (底层背景) -->
    <div v-if="isAnimeTheme" class="theme-background-layer"></div>
    
    <!-- Background Overlay with Blur Effect (顶层覆盖虚化层) -->
    <div v-if="isAnimeTheme && isOverlayEnabled" class="theme-background-overlay"></div>
    
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
    
    <!-- Background Selector (一键切换背景) -->
    <BackgroundSelector v-if="isAnimeTheme && !isAdminRoute" />
  </div>
</template>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Theme Background Layer for anime-girls theme (底层背景) */
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

/* Background Overlay with Blur Effect (顶层虚化覆盖层) */
.theme-background-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  pointer-events: none;
  background-image: var(--theme-background-image);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  opacity: var(--theme-background-opacity, 0.85);
  -webkit-backdrop-filter: blur(var(--theme-background-blur, 8px));
  backdrop-filter: blur(var(--theme-background-blur, 8px));
  -webkit-mask-image: linear-gradient(
    to bottom,
    rgba(0,0,0,0.3) 0%,
    rgba(0,0,0,0.15) 20%,
    rgba(0,0,0,0.1) 50%,
    rgba(0,0,0,0.15) 80%,
    rgba(0,0,0,0.3) 100%
  );
  mask-image: linear-gradient(
    to bottom,
    rgba(0,0,0,0.3) 0%,
    rgba(0,0,0,0.15) 20%,
    rgba(0,0,0,0.1) 50%,
    rgba(0,0,0,0.15) 80%,
    rgba(0,0,0,0.3) 100%
  );
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

#app.background-bg6 .theme-background-layer {
  background: 
    linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

#app.background-bg7 .theme-background-layer {
  background: 
    linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

#app.background-bg8 .theme-background-layer {
  background: 
    linear-gradient(180deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
}

#app.background-bg9 .theme-background-layer {
  background: 
    linear-gradient(135deg, #ff6b6b 0%, #ff8e53 50%, #feca57 100%);
}

#app.background-bg10 .theme-background-layer {
  background: 
    linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

#app.background-bg11 .theme-background-layer {
  background: 
    linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

#app.background-bg12 .theme-background-layer {
  background: 
    linear-gradient(135deg, #ee9ca7 0%, #ffdde1 100%);
}

#app.background-bg13 .theme-background-layer {
  background: 
    linear-gradient(180deg, #4facfe 0%, #00f2fe 100%);
}

#app.background-bg14 .theme-background-layer {
  background: 
    linear-gradient(135deg, #8b5cf6 0%, #a855f7 50%, #d946ef 100%);
}

#app.background-bg15 .theme-background-layer {
  background: 
    linear-gradient(180deg, #1e3c72 0%, #2a5298 50%, #0f2027 100%);
}

#app.background-bg16 .theme-background-layer {
  background: 
    linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

#app.background-bg17 .theme-background-layer {
  background: 
    linear-gradient(135deg, #0d0d0d 0%, #1a1a1a 50%, #2d2d2d 100%);
}

#app.background-bg18 .theme-background-layer {
  background: 
    linear-gradient(180deg, #2c1810 0%, #8b4513 40%, #daa520 100%);
}

#app.background-bg19 .theme-background-layer {
  background: 
    linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
}

#app.background-bg20 .theme-background-layer {
  background: 
    linear-gradient(135deg, #ff9a9e 0%, #fecfef 25%, #fad0c4 75%, #ffd1ff 100%);
}

.main-content {
  flex: 1;
  padding-top: 80px;
  position: relative;
  z-index: 1;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.main-content.no-padding {
  padding-top: 0;
}

/* Page transition overlay - kept for backwards compatibility */
.page-transition-overlay {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  transform: scaleY(0);
  transform-origin: bottom;
  z-index: 9999;
  pointer-events: none;
}

/* Vue transition classes - Fade in/out effect */
.fade-enter-active {
  transition: opacity 0.4s ease-out;
}

.fade-leave-active {
  transition: opacity 0.3s ease-in;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
