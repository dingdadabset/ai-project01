<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isLoginMode = ref(true)
const isSubmitting = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  nickname: ''
})

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
  errorMessage.value = ''
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    errorMessage.value = 'Please enter username and password'
    return
  }
  
  isSubmitting.value = true
  errorMessage.value = ''
  
  const result = await authStore.login(loginForm.username, loginForm.password)
  
  if (result.success) {
    router.push('/admin')
  } else {
    errorMessage.value = result.error || 'Login failed'
  }
  
  isSubmitting.value = false
}

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password || !registerForm.email) {
    errorMessage.value = 'Please fill in all required fields'
    return
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    errorMessage.value = 'Passwords do not match'
    return
  }
  
  if (registerForm.password.length < 6) {
    errorMessage.value = 'Password must be at least 6 characters'
    return
  }
  
  isSubmitting.value = true
  errorMessage.value = ''
  
  const result = await authStore.register({
    username: registerForm.username,
    password: registerForm.password,
    email: registerForm.email,
    nickname: registerForm.nickname
  })
  
  if (result.success) {
    router.push('/admin')
  } else {
    errorMessage.value = result.error || 'Registration failed'
  }
  
  isSubmitting.value = false
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="brand">
          <span class="brand-icon">✨</span>
          <span class="brand-text">Halo Blog</span>
        </div>
        <h1>{{ isLoginMode ? 'Welcome Back' : 'Create Account' }}</h1>
        <p>{{ isLoginMode ? 'Sign in to access the admin panel' : 'Register to start managing your blog' }}</p>
      </div>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      
      <!-- Login Form -->
      <form v-if="isLoginMode" @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="login-username">Username</label>
          <input 
            id="login-username"
            v-model="loginForm.username" 
            type="text" 
            class="form-input" 
            placeholder="Enter your username"
            autocomplete="username"
          />
        </div>
        
        <div class="form-group">
          <label for="login-password">Password</label>
          <input 
            id="login-password"
            v-model="loginForm.password" 
            type="password" 
            class="form-input" 
            placeholder="Enter your password"
            autocomplete="current-password"
          />
        </div>
        
        <button type="submit" class="btn btn-primary btn-full" :disabled="isSubmitting">
          {{ isSubmitting ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>
      
      <!-- Register Form -->
      <form v-else @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="register-username">Username *</label>
          <input 
            id="register-username"
            v-model="registerForm.username" 
            type="text" 
            class="form-input" 
            placeholder="Choose a username"
            autocomplete="username"
          />
        </div>
        
        <div class="form-group">
          <label for="register-email">Email *</label>
          <input 
            id="register-email"
            v-model="registerForm.email" 
            type="email" 
            class="form-input" 
            placeholder="Enter your email"
            autocomplete="email"
          />
        </div>
        
        <div class="form-group">
          <label for="register-nickname">Nickname</label>
          <input 
            id="register-nickname"
            v-model="registerForm.nickname" 
            type="text" 
            class="form-input" 
            placeholder="Your display name"
          />
        </div>
        
        <div class="form-group">
          <label for="register-password">Password *</label>
          <input 
            id="register-password"
            v-model="registerForm.password" 
            type="password" 
            class="form-input" 
            placeholder="Create a password"
            autocomplete="new-password"
          />
        </div>
        
        <div class="form-group">
          <label for="register-confirm">Confirm Password *</label>
          <input 
            id="register-confirm"
            v-model="registerForm.confirmPassword" 
            type="password" 
            class="form-input" 
            placeholder="Confirm your password"
            autocomplete="new-password"
          />
        </div>
        
        <button type="submit" class="btn btn-primary btn-full" :disabled="isSubmitting">
          {{ isSubmitting ? 'Creating account...' : 'Create Account' }}
        </button>
      </form>
      
      <div class="auth-footer">
        <p v-if="isLoginMode">
          Don't have an account? 
          <button type="button" class="link-btn" @click="toggleMode">Register</button>
        </p>
        <p v-else>
          Already have an account? 
          <button type="button" class="link-btn" @click="toggleMode">Sign In</button>
        </p>
        
        <router-link to="/" class="back-link">← Back to Site</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
}

.auth-container {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
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

.auth-header h1 {
  font-size: 1.75rem;
  margin-bottom: 8px;
}

.auth-header p {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.error-message {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #ef4444;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  font-size: 0.9rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.btn {
  padding: 14px 24px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  font-size: 1rem;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(99, 102, 241, 0.5);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-full {
  width: 100%;
}

.auth-footer {
  margin-top: 24px;
  text-align: center;
}

.auth-footer p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 16px;
}

.link-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  font-weight: 600;
  cursor: pointer;
  font-size: inherit;
}

.link-btn:hover {
  text-decoration: underline;
}

.back-link {
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.85rem;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: var(--color-primary);
}
</style>
