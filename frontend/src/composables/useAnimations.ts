import { onMounted, onUnmounted, ref } from 'vue'
import gsap from 'gsap'

// Scroll parallax composable
export function useParallax(selector: string, options: { speed?: number; direction?: 'up' | 'down' } = {}) {
  const { speed = 0.5, direction = 'up' } = options
  
  onMounted(() => {
    const elements = document.querySelectorAll(selector)
    
    const handleScroll = () => {
      const scrollY = window.scrollY
      
      elements.forEach((el) => {
        const rect = el.getBoundingClientRect()
        const elementTop = rect.top + scrollY
        const offset = (scrollY - elementTop) * speed
        const yValue = direction === 'up' ? -offset : offset
        
        gsap.to(el, {
          y: yValue,
          duration: 0.3,
          ease: 'power2.out'
        })
      })
    }
    
    window.addEventListener('scroll', handleScroll, { passive: true })
    
    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
    })
  })
}

// Card tilt effect on hover
export function useCardTilt(elementRef: HTMLElement | null, options: { max?: number; scale?: number } = {}) {
  const { max = 15, scale = 1.05 } = options
  
  if (!elementRef) return
  
  const handleMouseMove = (e: MouseEvent) => {
    const rect = elementRef.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    const centerX = rect.width / 2
    const centerY = rect.height / 2
    
    const rotateX = ((y - centerY) / centerY) * -max
    const rotateY = ((x - centerX) / centerX) * max
    
    gsap.to(elementRef, {
      rotateX,
      rotateY,
      scale,
      duration: 0.3,
      ease: 'power2.out'
    })
  }
  
  const handleMouseLeave = () => {
    gsap.to(elementRef, {
      rotateX: 0,
      rotateY: 0,
      scale: 1,
      duration: 0.5,
      ease: 'power2.out'
    })
  }
  
  elementRef.addEventListener('mousemove', handleMouseMove)
  elementRef.addEventListener('mouseleave', handleMouseLeave)
  
  return () => {
    elementRef.removeEventListener('mousemove', handleMouseMove)
    elementRef.removeEventListener('mouseleave', handleMouseLeave)
  }
}

// Page transition animation
export function usePageTransition() {
  const isTransitioning = ref(false)
  
  const startTransition = () => {
    isTransitioning.value = true
    return gsap.to('.page-transition-overlay', {
      scaleY: 1,
      duration: 0.5,
      ease: 'power4.inOut'
    })
  }
  
  const endTransition = () => {
    return gsap.to('.page-transition-overlay', {
      scaleY: 0,
      duration: 0.5,
      ease: 'power4.inOut',
      onComplete: () => {
        isTransitioning.value = false
      }
    })
  }
  
  return {
    isTransitioning,
    startTransition,
    endTransition
  }
}

// Scroll reveal animation
export function useScrollReveal(selector: string, options: { 
  threshold?: number
  stagger?: number 
  from?: gsap.TweenVars 
} = {}) {
  const { threshold = 0.1, stagger = 0.1, from = { opacity: 0, y: 50 } } = options
  
  onMounted(() => {
    const elements = document.querySelectorAll(selector)
    
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            gsap.fromTo(
              entry.target,
              from,
              {
                opacity: 1,
                y: 0,
                duration: 0.8,
                ease: 'power3.out'
              }
            )
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold }
    )
    
    elements.forEach((el) => {
      gsap.set(el, from)
      observer.observe(el)
    })
    
    onUnmounted(() => {
      observer.disconnect()
    })
  })
}

// Text reveal animation
export function useTextReveal(selector: string, options: { delay?: number } = {}) {
  const { delay = 0 } = options
  
  onMounted(() => {
    const elements = document.querySelectorAll(selector)
    
    elements.forEach((el, index) => {
      gsap.fromTo(
        el,
        { 
          opacity: 0, 
          y: 20,
          filter: 'blur(10px)'
        },
        {
          opacity: 1,
          y: 0,
          filter: 'blur(0px)',
          duration: 0.8,
          delay: delay + index * 0.1,
          ease: 'power3.out'
        }
      )
    })
  })
}

// Magnetic button effect
export function useMagneticEffect(elementRef: HTMLElement | null, strength: number = 0.3) {
  if (!elementRef) return
  
  const handleMouseMove = (e: MouseEvent) => {
    const rect = elementRef.getBoundingClientRect()
    const x = e.clientX - rect.left - rect.width / 2
    const y = e.clientY - rect.top - rect.height / 2
    
    gsap.to(elementRef, {
      x: x * strength,
      y: y * strength,
      duration: 0.3,
      ease: 'power2.out'
    })
  }
  
  const handleMouseLeave = () => {
    gsap.to(elementRef, {
      x: 0,
      y: 0,
      duration: 0.5,
      ease: 'elastic.out(1, 0.3)'
    })
  }
  
  elementRef.addEventListener('mousemove', handleMouseMove)
  elementRef.addEventListener('mouseleave', handleMouseLeave)
  
  return () => {
    elementRef.removeEventListener('mousemove', handleMouseMove)
    elementRef.removeEventListener('mouseleave', handleMouseLeave)
  }
}

// Floating animation
export function useFloatingAnimation(selector: string) {
  onMounted(() => {
    const elements = document.querySelectorAll(selector)
    
    elements.forEach((el, index) => {
      gsap.to(el, {
        y: 'random(-20, 20)',
        x: 'random(-10, 10)',
        rotation: 'random(-5, 5)',
        duration: 'random(3, 5)',
        repeat: -1,
        yoyo: true,
        ease: 'sine.inOut',
        delay: index * 0.2
      })
    })
  })
}
