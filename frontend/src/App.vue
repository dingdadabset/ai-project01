<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import NavBar from '@/components/layout/NavBar.vue'
import FooterBar from '@/components/layout/FooterBar.vue'
import gsap from 'gsap'

const router = useRouter()

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
    
    <!-- Navigation -->
    <NavBar />
    
    <!-- Main Content -->
    <main class="main-content">
      <RouterView v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    
    <!-- Footer -->
    <FooterBar />
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
