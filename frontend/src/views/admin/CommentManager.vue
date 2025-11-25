<script setup lang="ts">
import { onMounted, ref } from 'vue'
import commentApi from '@/api/comments'
import type { Comment } from '@/types'

const comments = ref<Comment[]>([])
const isLoading = ref(true)

onMounted(async () => {
  // Would need a list all comments API
  isLoading.value = false
})

const approveComment = async (id: number) => {
  await commentApi.approve(id)
}

const deleteComment = async (id: number) => {
  if (confirm('Delete this comment?')) {
    await commentApi.delete(id)
    comments.value = comments.value.filter(c => c.id !== id)
  }
}
</script>

<template>
  <div class="comment-manager">
    <header class="page-header">
      <h1>💬 Comments</h1>
      <p>Manage comments</p>
    </header>
    
    <div v-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
    </div>
    
    <div v-else-if="comments.length === 0" class="empty-state">
      <span class="empty-icon">💬</span>
      <h3>No comments yet</h3>
      <p>Comments will appear here once users start commenting</p>
    </div>
    
    <div v-else class="comments-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <div class="comment-content">
          <div class="comment-header">
            <span class="author">{{ comment.guestName || 'Guest' }}</span>
            <span class="status" :class="comment.status.toLowerCase()">
              {{ comment.status }}
            </span>
          </div>
          <p>{{ comment.content }}</p>
        </div>
        <div class="comment-actions">
          <button v-if="comment.status === 'PENDING'" class="btn btn-primary btn-sm" @click="approveComment(comment.id)">
            Approve
          </button>
          <button class="btn btn-danger btn-sm" @click="deleteComment(comment.id)">
            Delete
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 32px;
}

.page-header h1 { margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.empty-state {
  text-align: center;
  padding: 80px 0;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 24px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  justify-content: space-between;
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.author {
  font-weight: 600;
}

.status {
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 0.65rem;
  text-transform: uppercase;
}

.status.pending {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
}

.status.approved {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.comment-content p {
  color: var(--text-secondary);
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.75rem;
}

.btn-danger {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: none;
}
</style>
