<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useAppStore } from '@/stores/app'
import commentApi from '@/api/comments'
import type { Comment } from '@/types'
import gsap from 'gsap'

const route = useRoute()
const store = useAppStore()
const comments = ref<Comment[]>([])
const newComment = ref({
  content: '',
  guestName: '',
  guestEmail: ''
})
const isSubmitting = ref(false)

const slug = computed(() => route.params.slug as string)

onMounted(async () => {
  await store.fetchPost(slug.value)
  
  if (store.currentPost) {
    const response = await commentApi.getByPost(store.currentPost.id)
    comments.value = response.data.records || []
  }
  
  // Animate page entrance
  const tl = gsap.timeline()
  
  tl.fromTo('.post-hero',
    { opacity: 0 },
    { opacity: 1, duration: 0.8, ease: 'power3.out' }
  )
  .fromTo('.post-title',
    { opacity: 0, y: 50, filter: 'blur(10px)' },
    { opacity: 1, y: 0, filter: 'blur(0px)', duration: 0.8, ease: 'power3.out' },
    '-=0.4'
  )
  .fromTo('.post-content',
    { opacity: 0, y: 30 },
    { opacity: 1, y: 0, duration: 0.6, ease: 'power3.out' },
    '-=0.3'
  )
})

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const submitComment = async () => {
  if (!store.currentPost || !newComment.value.content) return
  
  isSubmitting.value = true
  try {
    await commentApi.create({
      postId: store.currentPost.id,
      content: newComment.value.content,
      guestName: newComment.value.guestName,
      guestEmail: newComment.value.guestEmail
    })
    
    // Refresh comments
    const response = await commentApi.getByPost(store.currentPost.id)
    comments.value = response.data.records || []
    
    // Reset form
    newComment.value = { content: '', guestName: '', guestEmail: '' }
  } catch (error) {
    console.error('Failed to submit comment:', error)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="post-detail">
    <!-- Loading State -->
    <div v-if="store.isLoading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading post...</p>
    </div>
    
    <template v-else-if="store.currentPost">
      <!-- Hero Section -->
      <section class="post-hero">
        <div class="hero-bg">
          <img 
            :src="store.currentPost.thumbnail || 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1600'" 
            :alt="store.currentPost.title"
          />
          <div class="hero-overlay"></div>
        </div>
        
        <div class="hero-content container">
          <div class="post-meta">
            <span v-if="store.currentPost.categoryName" class="category">
              {{ store.currentPost.categoryName }}
            </span>
            <span class="date">{{ formatDate(store.currentPost.createdAt) }}</span>
          </div>
          
          <h1 class="post-title">{{ store.currentPost.title }}</h1>
          
          <div v-if="store.currentPost.summary" class="post-summary">
            {{ store.currentPost.summary }}
          </div>
          
          <div class="post-author">
            <div class="author-avatar">
              {{ store.currentPost.authorName?.charAt(0) || 'A' }}
            </div>
            <div class="author-info">
              <span class="author-name">{{ store.currentPost.authorName || 'Anonymous' }}</span>
              <span class="read-time">5 min read</span>
            </div>
          </div>
        </div>
      </section>
      
      <!-- Post Content -->
      <article class="post-content section">
        <div class="container content-container">
          <div class="content-body" v-html="store.currentPost.content"></div>
          
          <!-- Tags -->
          <div v-if="store.currentPost.tags?.length" class="post-tags">
            <span class="tags-label">Tags:</span>
            <RouterLink 
              v-for="tag in store.currentPost.tags" 
              :key="tag"
              :to="`/tags/${tag}`"
              class="tag"
            >
              {{ tag }}
            </RouterLink>
          </div>
          
          <!-- Stats -->
          <div class="post-stats">
            <div class="stat">
              <span class="stat-icon">👁</span>
              <span>{{ store.currentPost.viewCount }} views</span>
            </div>
            <div class="stat">
              <span class="stat-icon">❤️</span>
              <span>{{ store.currentPost.likeCount }} likes</span>
            </div>
            <div class="stat">
              <span class="stat-icon">💬</span>
              <span>{{ store.currentPost.commentCount }} comments</span>
            </div>
          </div>
          
          <!-- Share -->
          <div class="share-section">
            <span class="share-label">Share this post:</span>
            <div class="share-buttons">
              <button class="share-btn twitter">Twitter</button>
              <button class="share-btn facebook">Facebook</button>
              <button class="share-btn copy">Copy Link</button>
            </div>
          </div>
        </div>
      </article>
      
      <!-- Comments Section -->
      <section class="comments-section section">
        <div class="container content-container">
          <h2>
            <span class="section-icon">💬</span>
            Comments ({{ comments.length }})
          </h2>
          
          <!-- Comment Form -->
          <div v-if="store.currentPost.allowComment" class="comment-form glass">
            <h3>Leave a Comment</h3>
            <div class="form-row">
              <input 
                v-model="newComment.guestName"
                type="text" 
                class="input" 
                placeholder="Your name"
              />
              <input 
                v-model="newComment.guestEmail"
                type="email" 
                class="input" 
                placeholder="Your email"
              />
            </div>
            <textarea 
              v-model="newComment.content"
              class="input" 
              rows="4" 
              placeholder="Write your comment..."
            ></textarea>
            <button 
              class="btn btn-primary"
              :disabled="isSubmitting || !newComment.content"
              @click="submitComment"
            >
              {{ isSubmitting ? 'Posting...' : 'Post Comment' }}
            </button>
          </div>
          
          <!-- Comments List -->
          <div class="comments-list">
            <div 
              v-for="comment in comments" 
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-avatar">
                {{ comment.guestName?.charAt(0) || 'G' }}
              </div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.guestName || 'Guest' }}</span>
                  <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
              </div>
            </div>
            
            <div v-if="comments.length === 0" class="no-comments">
              <span class="empty-icon">💭</span>
              <p>No comments yet. Be the first to share your thoughts!</p>
            </div>
          </div>
        </div>
      </section>
    </template>
    
    <!-- Not Found -->
    <div v-else class="not-found">
      <span class="not-found-icon">😕</span>
      <h2>Post not found</h2>
      <RouterLink to="/posts" class="btn btn-primary">
        Back to Posts
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.post-detail {
  min-height: 100vh;
}

.loading-container,
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.not-found-icon {
  font-size: 6rem;
  margin-bottom: 24px;
}

/* Hero Section */
.post-hero {
  position: relative;
  min-height: 70vh;
  display: flex;
  align-items: flex-end;
  padding: 80px 0;
}

.hero-bg {
  position: absolute;
  inset: 0;
}

.hero-bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, 
    rgba(15, 15, 35, 0.3) 0%,
    rgba(15, 15, 35, 0.8) 50%,
    rgba(15, 15, 35, 1) 100%
  );
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.category {
  padding: 6px 16px;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 600;
}

.date {
  color: var(--text-secondary);
}

.post-title {
  font-size: clamp(2rem, 5vw, 3.5rem);
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 24px;
}

.post-summary {
  font-size: 1.25rem;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 32px;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.25rem;
}

.author-name {
  display: block;
  font-weight: 600;
}

.read-time {
  color: var(--text-muted);
  font-size: 0.875rem;
}

/* Post Content */
.content-container {
  max-width: 800px;
}

.content-body {
  font-size: 1.125rem;
  line-height: 1.8;
  color: var(--text-secondary);
}

.content-body :deep(h2) {
  font-size: 1.75rem;
  margin: 48px 0 24px;
  color: var(--text-primary);
}

.content-body :deep(h3) {
  font-size: 1.5rem;
  margin: 36px 0 18px;
  color: var(--text-primary);
}

.content-body :deep(p) {
  margin-bottom: 24px;
}

.content-body :deep(ul),
.content-body :deep(ol) {
  margin: 24px 0;
  padding-left: 32px;
}

.content-body :deep(li) {
  margin-bottom: 12px;
}

.content-body :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
}

.content-body :deep(code) {
  background: var(--bg-secondary);
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
}

.content-body :deep(pre) {
  background: var(--bg-secondary);
  padding: 24px;
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: 24px 0;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.tags-label {
  color: var(--text-muted);
}

.post-stats {
  display: flex;
  gap: 32px;
  margin-top: 32px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}

.stat-icon {
  font-size: 1.25rem;
}

.share-section {
  margin-top: 48px;
  padding: 32px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
}

.share-label {
  display: block;
  margin-bottom: 16px;
  font-weight: 600;
}

.share-buttons {
  display: flex;
  gap: 12px;
}

.share-btn {
  padding: 10px 20px;
  border-radius: var(--radius-md);
  border: none;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.share-btn.twitter {
  background: #1da1f2;
  color: white;
}

.share-btn.facebook {
  background: #4267B2;
  color: white;
}

.share-btn.copy {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.share-btn:hover {
  transform: translateY(-2px);
  opacity: 0.9;
}

/* Comments Section */
.comments-section h2 {
  font-size: 1.75rem;
  margin-bottom: 32px;
}

.section-icon {
  margin-right: 12px;
}

.comment-form {
  padding: 32px;
  border-radius: var(--radius-lg);
  margin-bottom: 48px;
}

.comment-form h3 {
  font-size: 1.25rem;
  margin-bottom: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.comment-form textarea {
  resize: vertical;
  margin-bottom: 16px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
}

.comment-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
}

.comment-date {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.comment-content {
  color: var(--text-secondary);
  line-height: 1.6;
}

.no-comments {
  text-align: center;
  padding: 48px;
  color: var(--text-muted);
}

.no-comments .empty-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .post-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .share-buttons {
    flex-wrap: wrap;
  }
}
</style>
