<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useAppStore } from '@/stores/app'
import postApi from '@/api/posts'
import MarkdownEditor from '@/components/common/MarkdownEditor.vue'
import type { Post, PostRequest } from '@/types'

const store = useAppStore()
const showModal = ref(false)
const editingPost = ref<Post | null>(null)
const isSubmitting = ref(false)

const formData = reactive<PostRequest>({
  title: '',
  summary: '',
  content: '',
  status: 'DRAFT',
  categoryId: undefined,
  tags: [],
  allowComment: true
})

onMounted(async () => {
  await Promise.all([
    store.fetchPosts(0, 50),
    store.fetchCategories(),
    store.fetchTags()
  ])
})

const openCreateModal = () => {
  editingPost.value = null
  Object.assign(formData, {
    title: '',
    summary: '',
    content: '',
    status: 'DRAFT',
    categoryId: undefined,
    tags: [],
    allowComment: true
  })
  showModal.value = true
}

const openEditModal = (post: Post) => {
  editingPost.value = post
  Object.assign(formData, {
    title: post.title,
    summary: post.summary || '',
    content: post.content || '',
    status: post.status,
    categoryId: post.categoryId,
    tags: post.tags || [],
    allowComment: post.allowComment
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingPost.value = null
}

const submitPost = async () => {
  if (!formData.title.trim()) {
    alert('Please enter a title')
    return
  }
  
  isSubmitting.value = true
  try {
    if (editingPost.value) {
      await postApi.update(editingPost.value.id, formData)
    } else {
      await postApi.create(formData)
    }
    await store.fetchPosts(0, 50)
    closeModal()
  } catch (error) {
    console.error('Failed to save post:', error)
    alert('Failed to save post')
  } finally {
    isSubmitting.value = false
  }
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
                <button class="action-btn edit" @click="openEditModal(post)">Edit</button>
                <button class="action-btn delete" @click="deletePost(post.id)">
                  Delete
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal for creating/editing posts -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingPost ? 'Edit Post' : 'Create New Post' }}</h2>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label for="title">Title *</label>
            <input 
              id="title"
              v-model="formData.title" 
              type="text" 
              class="form-input" 
              placeholder="Enter post title"
            />
          </div>
          
          <div class="form-group">
            <label for="summary">Summary</label>
            <textarea 
              id="summary"
              v-model="formData.summary" 
              class="form-input" 
              rows="2"
              placeholder="Brief summary of the post"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label for="content">Content (Markdown)</label>
            <MarkdownEditor 
              v-model="formData.content"
              placeholder="Write your post content in Markdown format..."
            />
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="status">Status</label>
              <select id="status" v-model="formData.status" class="form-input">
                <option value="DRAFT">Draft</option>
                <option value="PUBLISHED">Published</option>
                <option value="PRIVATE">Private</option>
              </select>
            </div>
            
            <div class="form-group">
              <label for="category">Category</label>
              <select id="category" v-model="formData.categoryId" class="form-input">
                <option :value="undefined">No Category</option>
                <option v-for="cat in store.categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
          </div>
          
          <div class="form-group">
            <label>
              <input type="checkbox" v-model="formData.allowComment" />
              Allow Comments
            </label>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">Cancel</button>
          <button 
            class="btn btn-primary" 
            @click="submitPost"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? 'Saving...' : (editingPost ? 'Update' : 'Create') }}
          </button>
        </div>
      </div>
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
  flex-wrap: wrap;
  gap: 16px;
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
  text-transform: uppercase;
  white-space: nowrap;
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
  white-space: nowrap;
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
  flex-wrap: wrap;
}

.action-btn {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: none;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
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
  max-width: 700px;
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

.content-editor {
  font-family: 'Monaco', 'Menlo', monospace;
  resize: vertical;
  min-height: 200px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group input[type="checkbox"] {
  margin-right: 8px;
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
  
  .post-title {
    max-width: 150px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
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

@media (max-width: 480px) {
  .action-btn {
    padding: 4px 8px;
  }
}
</style>
