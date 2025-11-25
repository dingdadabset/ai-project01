<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { RouterView, useRouter, useRoute } from 'vue-router'
import NavBar from '@/components/layout/NavBar.vue'
import FooterBar from '@/components/layout/FooterBar.vue'
import gsap from 'gsap'

const router = useRouter()
const route = useRoute()

// Check if current route is admin or login
const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin') || route.path === '/login'
})

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

onMounted(() => {
  // Initial page load animation
  gsap.fromTo(
    '.main-content',
    { opacity: 0 },
    { opacity: 1, duration: 0.5, ease: 'power2.out' }
  )
})
</script>

<template>
  <div id="app">
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
}

.main-content {
  flex: 1;
  padding-top: 80px;
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
