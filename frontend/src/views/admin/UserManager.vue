<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import userApi from '@/api/users'
import type { User } from '@/types'

const users = ref<User[]>([])
const isLoading = ref(true)
const showModal = ref(false)
const editingUser = ref<User | null>(null)
const isSubmitting = ref(false)

const formData = reactive({
  username: '',
  password: '',
  email: '',
  nickname: '',
  description: '',
  role: 'SUBSCRIBER' as 'ADMIN' | 'AUTHOR' | 'SUBSCRIBER'
})

onMounted(async () => {
  await loadUsers()
})

const loadUsers = async () => {
  try {
    const response = await userApi.list()
    users.value = response.data || []
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    isLoading.value = false
  }
}

const openCreateModal = () => {
  editingUser.value = null
  Object.assign(formData, {
    username: '',
    password: '',
    email: '',
    nickname: '',
    description: '',
    role: 'SUBSCRIBER'
  })
  showModal.value = true
}

const openEditModal = (user: User) => {
  editingUser.value = user
  Object.assign(formData, {
    username: user.username,
    password: '',
    email: user.email,
    nickname: user.nickname || '',
    description: user.description || '',
    role: user.role
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingUser.value = null
}

const submitUser = async () => {
  if (!formData.username.trim() || !formData.email.trim()) {
    alert('Please enter username and email')
    return
  }
  
  if (!editingUser.value && !formData.password.trim()) {
    alert('Please enter a password')
    return
  }
  
  isSubmitting.value = true
  try {
    if (editingUser.value) {
      await userApi.update(editingUser.value.id, {
        nickname: formData.nickname,
        description: formData.description
      })
    } else {
      await userApi.create({
        username: formData.username,
        password: formData.password,
        email: formData.email,
        nickname: formData.nickname,
        description: formData.description,
        role: formData.role
      })
    }
    await loadUsers()
    closeModal()
  } catch (error) {
    console.error('Failed to save user:', error)
    alert('Failed to save user')
  } finally {
    isSubmitting.value = false
  }
}

const deleteUser = async (id: number) => {
  if (confirm('Delete this user?')) {
    await userApi.delete(id)
    users.value = users.value.filter(u => u.id !== id)
  }
}
</script>

<template>
  <div class="user-manager">
    <header class="page-header">
      <div>
        <h1>👥 Users</h1>
        <p>Manage users</p>
      </div>
      <button class="btn btn-primary" @click="openCreateModal">
        + New User
      </button>
    </header>
    
    <div v-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
    </div>
    
    <div v-else class="users-table">
      <table>
        <thead>
          <tr>
            <th>User</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>
              <div class="user-info">
                <div class="user-avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
                <div>
                  <div class="username">{{ user.username }}</div>
                  <div class="nickname">{{ user.nickname }}</div>
                </div>
              </div>
            </td>
            <td>{{ user.email }}</td>
            <td>
              <span class="role" :class="user.role.toLowerCase()">
                {{ user.role }}
              </span>
            </td>
            <td>
              <span class="status" :class="user.status.toLowerCase()">
                {{ user.status }}
              </span>
            </td>
            <td>
              <div class="actions">
                <button class="action-btn edit" @click="openEditModal(user)">Edit</button>
                <button class="action-btn delete" @click="deleteUser(user.id)">
                  Delete
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal for creating/editing users -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingUser ? 'Edit User' : 'Create New User' }}</h2>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label for="username">Username *</label>
            <input 
              id="username"
              v-model="formData.username" 
              type="text" 
              class="form-input" 
              placeholder="Enter username"
              :disabled="!!editingUser"
            />
          </div>
          
          <div class="form-group" v-if="!editingUser">
            <label for="password">Password *</label>
            <input 
              id="password"
              v-model="formData.password" 
              type="password" 
              class="form-input" 
              placeholder="Enter password"
            />
          </div>
          
          <div class="form-group">
            <label for="email">Email *</label>
            <input 
              id="email"
              v-model="formData.email" 
              type="email" 
              class="form-input" 
              placeholder="Enter email"
              :disabled="!!editingUser"
            />
          </div>
          
          <div class="form-group">
            <label for="nickname">Nickname</label>
            <input 
              id="nickname"
              v-model="formData.nickname" 
              type="text" 
              class="form-input" 
              placeholder="Display name"
            />
          </div>
          
          <div class="form-group">
            <label for="description">Description</label>
            <textarea 
              id="description"
              v-model="formData.description" 
              class="form-input" 
              rows="3"
              placeholder="Brief bio"
            ></textarea>
          </div>
          
          <div class="form-group" v-if="!editingUser">
            <label for="role">Role</label>
            <select id="role" v-model="formData.role" class="form-input">
              <option value="SUBSCRIBER">Subscriber</option>
              <option value="AUTHOR">Author</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">Cancel</button>
          <button 
            class="btn btn-primary" 
            @click="submitUser"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Saving...' : (editingUser ? 'Update' : 'Create') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h1 { margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.users-table {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}

th, td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

th {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.875rem;
  white-space: nowrap;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.username {
  font-weight: 500;
}

.nickname {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.role, .status {
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.role.admin { background: rgba(99, 102, 241, 0.2); color: #6366f1; }
.role.author { background: rgba(139, 92, 246, 0.2); color: #8b5cf6; }
.role.subscriber { background: rgba(16, 185, 129, 0.2); color: #10b981; }

.status.active { background: rgba(16, 185, 129, 0.2); color: #10b981; }
.status.inactive { background: rgba(251, 191, 36, 0.2); color: #fbbf24; }
.status.banned { background: rgba(239, 68, 68, 0.2); color: #ef4444; }

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: none;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
}

.action-btn.edit {
  background: var(--color-primary);
  color: white;
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.action-btn:hover {
  opacity: 0.8;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--text-muted);
  cursor: pointer;
  line-height: 1;
}

.close-btn:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-secondary);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* Mobile responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .page-header h1 {
    font-size: 1.5rem;
  }
  
  th, td {
    padding: 12px 8px;
    font-size: 0.875rem;
  }
  
  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: 0.75rem;
  }
  
  .modal-content {
    margin: 10px;
  }
  
  .modal-body {
    padding: 16px;
  }
  
  .modal-header,
  .modal-footer {
    padding: 16px;
  }
}
</style>
