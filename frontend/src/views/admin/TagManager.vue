<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import tagApi from '@/api/tags'

const store = useAppStore()
const newTagName = ref('')
const showForm = ref(false)

onMounted(async () => {
  await store.fetchTags()
})

const createTag = async () => {
  if (!newTagName.value) return
  await tagApi.create({ name: newTagName.value })
  await store.fetchTags()
  newTagName.value = ''
  showForm.value = false
}

const deleteTag = async (id: number) => {
  if (confirm('Delete this tag?')) {
    await tagApi.delete(id)
    await store.fetchTags()
  }
}
</script>

<template>
  <div class="tag-manager">
    <header class="page-header">
      <div>
        <h1>🏷️ Tags</h1>
        <p>Manage tags</p>
      </div>
      <button class="btn btn-primary" @click="showForm = !showForm">
        {{ showForm ? 'Cancel' : '+ New Tag' }}
      </button>
    </header>
    
    <div v-if="showForm" class="form-card">
      <input v-model="newTagName" class="input" placeholder="Tag name" />
      <button class="btn btn-primary" @click="createTag">Create</button>
    </div>
    
    <div class="tags-cloud">
      <div v-for="tag in store.tags" :key="tag.id" class="tag-item">
        <span class="tag-name"># {{ tag.name }}</span>
        <span class="tag-count">{{ tag.postCount }}</span>
        <button class="delete-btn" @click="deleteTag(tag.id)">×</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-header h1 { margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.form-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  margin-bottom: 32px;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

.tag-name {
  font-weight: 600;
  color: var(--color-primary);
}

.tag-count {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.delete-btn {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
}
</style>
