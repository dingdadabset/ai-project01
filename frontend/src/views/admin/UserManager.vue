<script setup lang="ts">
import { onMounted, ref } from 'vue'
import userApi from '@/api/users'
import type { User } from '@/types'

const users = ref<User[]>([])
const isLoading = ref(true)

onMounted(async () => {
  try {
    const response = await userApi.list()
    users.value = response.data || []
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    isLoading.value = false
  }
})

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
      <h1>👥 Users</h1>
      <p>Manage users</p>
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
                <button class="action-btn edit">Edit</button>
                <button class="action-btn delete" @click="deleteUser(user.id)">
                  Delete
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 32px;
}

.page-header h1 { margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.users-table {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
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
}

.action-btn {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: none;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
}

.action-btn.edit {
  background: var(--color-primary);
  color: white;
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}
</style>
