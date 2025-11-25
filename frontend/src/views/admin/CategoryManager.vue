<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAppStore } from '@/stores/app'
import categoryApi from '@/api/categories'

const store = useAppStore()
const newCategory = ref({ name: '', description: '' })
const showForm = ref(false)

onMounted(async () => {
  await store.fetchCategories()
})

const createCategory = async () => {
  if (!newCategory.value.name) return
  await categoryApi.create(newCategory.value)
  await store.fetchCategories()
  newCategory.value = { name: '', description: '' }
  showForm.value = false
}

const deleteCategory = async (id: number) => {
  if (confirm('Delete this category?')) {
    await categoryApi.delete(id)
    await store.fetchCategories()
  }
}
</script>

<template>
  <div class="category-manager">
    <header class="page-header">
      <div>
        <h1>📁 Categories</h1>
        <p>Manage categories</p>
      </div>
      <button class="btn btn-primary" @click="showForm = !showForm">
        {{ showForm ? 'Cancel' : '+ New Category' }}
      </button>
    </header>
    
    <div v-if="showForm" class="form-card">
      <input v-model="newCategory.name" class="input" placeholder="Category name" />
      <input v-model="newCategory.description" class="input" placeholder="Description" />
      <button class="btn btn-primary" @click="createCategory">Create</button>
    </div>
    
    <div class="items-grid">
      <div v-for="cat in store.categories" :key="cat.id" class="item-card">
        <h3>{{ cat.name }}</h3>
        <p>{{ cat.description }}</p>
        <span class="count">{{ cat.postCount }} posts</span>
        <button class="delete-btn" @click="deleteCategory(cat.id)">×</button>
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

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 24px;
}

.item-card {
  position: relative;
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

.item-card h3 { margin-bottom: 8px; }
.item-card p { color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 12px; }
.count { color: var(--text-muted); font-size: 0.75rem; }

.delete-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  cursor: pointer;
}
</style>
