<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import postApi from '@/api/posts'

const store = useAppStore()
const showModal = ref(false)
const editingPost = ref<any>(null)

onMounted(async () => {
  await store.fetchPosts(0, 50)
})

const openCreateModal = () => {
  editingPost.value = { title: '', summary: '', content: '', status: 'DRAFT' }
  showModal.value = true
}

const deletePost = async (id: number) => {
  if (confirm('Are you sure you want to delete this post?')) {
    await postApi.delete(id)
    await store.fetchPosts(0, 50)
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString()
}
</script>

<template>
  <div class="post-manager">
    <header class="page-header">
      <div>
        <h1>📝 Posts</h1>
        <p>Manage your blog posts</p>
      </div>
      <button class="btn btn-primary" @click="openCreateModal">
        + New Post
      </button>
    </header>
    
    <div class="posts-table">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Status</th>
            <th>Category</th>
            <th>Views</th>
            <th>Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="post in store.posts" :key="post.id">
            <td>
              <div class="post-title">{{ post.title }}</div>
            </td>
            <td>
              <span class="status" :class="post.status.toLowerCase()">
                {{ post.status }}
              </span>
            </td>
            <td>{{ post.categoryName || '-' }}</td>
            <td>{{ post.viewCount }}</td>
            <td>{{ formatDate(post.createdAt) }}</td>
            <td>
              <div class="actions">
                <button class="action-btn edit">Edit</button>
                <button class="action-btn delete" @click="deletePost(post.id)">
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
.post-manager {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

.posts-table {
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
  text-transform: uppercase;
}

.post-title {
  font-weight: 500;
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status {
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 600;
}

.status.published {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.status.draft {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

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
  transition: all 0.3s ease;
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
</style>
