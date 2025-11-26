<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { marked } from 'marked'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const showPreview = ref(false)

const content = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

const renderedContent = computed(() => {
  if (!content.value) return ''
  return marked(content.value) as string
})

const togglePreview = () => {
  showPreview.value = !showPreview.value
}

const insertMarkdown = (prefix: string, suffix: string = '') => {
  const textarea = document.querySelector('.md-textarea') as HTMLTextAreaElement
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = content.value
  const selectedText = text.substring(start, end)
  
  const newText = text.substring(0, start) + prefix + selectedText + suffix + text.substring(end)
  content.value = newText
  
  // Restore cursor position
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selectedText.length)
  }, 0)
}
</script>

<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <div class="toolbar-buttons">
        <button type="button" class="toolbar-btn" @click="insertMarkdown('**', '**')" title="Bold">
          <strong>B</strong>
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('*', '*')" title="Italic">
          <em>I</em>
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('# ')" title="Heading">
          H
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('- ')" title="List">
          •
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('`', '`')" title="Code">
          &lt;/&gt;
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('[', '](url)')" title="Link">
          🔗
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('```\n', '\n```')" title="Code Block">
          { }
        </button>
      </div>
      <button 
        type="button" 
        class="preview-toggle"
        :class="{ active: showPreview }"
        @click="togglePreview"
      >
        {{ showPreview ? '✏️ Edit' : '👁️ Preview' }}
      </button>
    </div>
    
    <div class="editor-content">
      <textarea
        v-if="!showPreview"
        v-model="content"
        class="md-textarea"
        :placeholder="placeholder || 'Write your content in Markdown...'"
        rows="12"
      ></textarea>
      
      <div 
        v-else 
        class="md-preview"
        v-html="renderedContent"
      ></div>
    </div>
    
    <div class="editor-hint">
      💡 Supports Markdown syntax: **bold**, *italic*, # heading, - list, `code`
    </div>
  </div>
</template>

<style scoped>
.markdown-editor {
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-secondary);
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-buttons {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover {
  background: var(--color-primary);
  color: white;
}

.preview-toggle {
  padding: 6px 12px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
}

.preview-toggle:hover,
.preview-toggle.active {
  background: var(--color-primary);
  color: white;
}

.editor-content {
  min-height: 250px;
}

.md-textarea {
  width: 100%;
  min-height: 250px;
  padding: 16px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
}

.md-textarea:focus {
  outline: none;
}

.md-textarea::placeholder {
  color: var(--text-muted);
}

.md-preview {
  padding: 16px;
  min-height: 250px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.md-preview :deep(h1),
.md-preview :deep(h2),
.md-preview :deep(h3) {
  color: var(--text-primary);
  margin: 16px 0 8px;
}

.md-preview :deep(h1) { font-size: 1.75rem; }
.md-preview :deep(h2) { font-size: 1.5rem; }
.md-preview :deep(h3) { font-size: 1.25rem; }

.md-preview :deep(p) {
  margin-bottom: 12px;
}

.md-preview :deep(ul),
.md-preview :deep(ol) {
  padding-left: 24px;
  margin-bottom: 12px;
}

.md-preview :deep(li) {
  margin-bottom: 4px;
}

.md-preview :deep(code) {
  background: rgba(0, 0, 0, 0.3);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

.md-preview :deep(pre) {
  background: rgba(0, 0, 0, 0.3);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}

.md-preview :deep(pre code) {
  background: none;
  padding: 0;
}

.md-preview :deep(a) {
  color: var(--color-primary);
}

.md-preview :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  padding-left: 16px;
  margin: 12px 0;
  color: var(--text-muted);
}

.editor-hint {
  padding: 8px 12px;
  font-size: 12px;
  color: var(--text-muted);
  background: rgba(0, 0, 0, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

@media (max-width: 480px) {
  .toolbar-btn {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .preview-toggle {
    font-size: 11px;
    padding: 4px 8px;
  }
}
</style>
