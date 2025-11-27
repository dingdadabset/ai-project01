<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { marked } from 'marked'
import uploadApi from '@/api/uploads'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// View mode: 'edit', 'preview', 'split'
const viewMode = ref<'edit' | 'preview' | 'split'>('split')
const isUploading = ref(false)
const uploadError = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)

const content = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

const renderedContent = computed(() => {
  if (!content.value) return '<p class="empty-hint">预览将在此显示...</p>'
  return marked(content.value) as string
})

const setViewMode = (mode: 'edit' | 'preview' | 'split') => {
  viewMode.value = mode
}

const insertMarkdown = (prefix: string, suffix: string = '') => {
  const textarea = textareaRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = content.value || ''
  const selectedText = text.substring(start, end)
  
  const newText = text.substring(0, start) + prefix + selectedText + suffix + text.substring(end)
  content.value = newText
  
  // Restore cursor position
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selectedText.length)
  }, 0)
}

const insertHeading = (level: number) => {
  const prefix = '#'.repeat(level) + ' '
  insertAtLineStart(prefix)
}

const insertAtLineStart = (prefix: string) => {
  const textarea = textareaRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const text = content.value || ''
  
  // Find the start of the current line
  let lineStart = start
  while (lineStart > 0 && text[lineStart - 1] !== '\n') {
    lineStart--
  }
  
  const newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
  content.value = newText
  
  setTimeout(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length)
  }, 0)
}

const insertTable = () => {
  const tableMarkdown = `
| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 内容 | 内容 | 内容 |
| 内容 | 内容 | 内容 |
`
  insertMarkdown(tableMarkdown)
}

const insertQuote = () => {
  insertAtLineStart('> ')
}

const insertHorizontalRule = () => {
  insertMarkdown('\n---\n')
}

const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

const handleFileUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0) return
  
  isUploading.value = true
  uploadError.value = ''
  
  try {
    for (const file of files) {
      const response = await uploadApi.upload(file, 'images')
      if (response.data.success && response.data.url) {
        const imageMarkdown = `![${file.name}](${response.data.url})`
        insertMarkdown(imageMarkdown)
      } else {
        uploadError.value = response.data.error || '上传失败'
      }
    }
  } catch (error) {
    uploadError.value = '上传图片失败，请重试'
    console.error('Upload failed:', error)
  } finally {
    isUploading.value = false
    input.value = '' // Reset input
  }
}

const handlePaste = async (event: ClipboardEvent) => {
  const items = event.clipboardData?.items
  if (!items) return
  
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      const file = item.getAsFile()
      if (!file) continue
      
      isUploading.value = true
      uploadError.value = ''
      
      try {
        const response = await uploadApi.upload(file, 'images')
        if (response.data.success && response.data.url) {
          const imageMarkdown = `![粘贴的图片](${response.data.url})`
          insertMarkdown(imageMarkdown)
        } else {
          uploadError.value = response.data.error || '上传失败'
        }
      } catch (error) {
        uploadError.value = '上传图片失败，请重试'
        console.error('Upload failed:', error)
      } finally {
        isUploading.value = false
      }
      break
    }
  }
}

const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return
  
  isUploading.value = true
  uploadError.value = ''
  
  try {
    for (const file of files) {
      if (file.type.startsWith('image/')) {
        const response = await uploadApi.upload(file, 'images')
        if (response.data.success && response.data.url) {
          const imageMarkdown = `![${file.name}](${response.data.url})`
          insertMarkdown(imageMarkdown)
        } else {
          uploadError.value = response.data.error || '上传失败'
        }
      }
    }
  } catch (error) {
    uploadError.value = '上传图片失败，请重试'
    console.error('Upload failed:', error)
  } finally {
    isUploading.value = false
  }
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
}
</script>

<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <div class="toolbar-buttons">
        <button type="button" class="toolbar-btn" @click="insertMarkdown('**', '**')" title="加粗 (Ctrl+B)">
          <strong>B</strong>
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('*', '*')" title="斜体 (Ctrl+I)">
          <em>I</em>
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('~~', '~~')" title="删除线">
          <s>S</s>
        </button>
        <span class="toolbar-divider"></span>
        <button type="button" class="toolbar-btn" @click="insertHeading(1)" title="标题1">
          H1
        </button>
        <button type="button" class="toolbar-btn" @click="insertHeading(2)" title="标题2">
          H2
        </button>
        <button type="button" class="toolbar-btn" @click="insertHeading(3)" title="标题3">
          H3
        </button>
        <span class="toolbar-divider"></span>
        <button type="button" class="toolbar-btn" @click="insertAtLineStart('- ')" title="无序列表">
          •
        </button>
        <button type="button" class="toolbar-btn" @click="insertAtLineStart('1. ')" title="有序列表">
          1.
        </button>
        <button type="button" class="toolbar-btn" @click="insertAtLineStart('- [ ] ')" title="任务列表">
          ☑
        </button>
        <span class="toolbar-divider"></span>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('`', '`')" title="行内代码">
          &lt;/&gt;
        </button>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('```\n', '\n```')" title="代码块">
          { }
        </button>
        <button type="button" class="toolbar-btn" @click="insertQuote" title="引用">
          ❝
        </button>
        <span class="toolbar-divider"></span>
        <button type="button" class="toolbar-btn" @click="insertMarkdown('[链接文本](', ')')" title="链接">
          🔗
        </button>
        <button type="button" class="toolbar-btn" @click="triggerFileUpload" title="上传图片" :disabled="isUploading">
          {{ isUploading ? '⏳' : '🖼️' }}
        </button>
        <button type="button" class="toolbar-btn" @click="insertTable" title="表格">
          📊
        </button>
        <button type="button" class="toolbar-btn" @click="insertHorizontalRule" title="分割线">
          ─
        </button>
      </div>
      <div class="view-mode-toggle">
        <button 
          type="button" 
          class="mode-btn"
          :class="{ active: viewMode === 'edit' }"
          @click="setViewMode('edit')"
        >
          ✏️ 编辑
        </button>
        <button 
          type="button" 
          class="mode-btn"
          :class="{ active: viewMode === 'split' }"
          @click="setViewMode('split')"
        >
          📋 分屏
        </button>
        <button 
          type="button" 
          class="mode-btn"
          :class="{ active: viewMode === 'preview' }"
          @click="setViewMode('preview')"
        >
          👁️ 预览
        </button>
      </div>
    </div>
    
    <!-- Hidden file input -->
    <input 
      ref="fileInputRef"
      type="file" 
      accept="image/*" 
      multiple 
      hidden 
      @change="handleFileUpload"
    />
    
    <!-- Upload error message -->
    <div v-if="uploadError" class="upload-error">
      {{ uploadError }}
      <button @click="uploadError = ''">&times;</button>
    </div>
    
    <div class="editor-content" :class="viewMode">
      <!-- Edit pane -->
      <div 
        v-if="viewMode !== 'preview'" 
        class="edit-pane"
        @drop="handleDrop"
        @dragover="handleDragOver"
      >
        <textarea
          ref="textareaRef"
          v-model="content"
          class="md-textarea"
          :placeholder="placeholder || '使用 Markdown 格式编写内容...\n\n支持拖拽或粘贴图片上传'"
          @paste="handlePaste"
        ></textarea>
        <div v-if="isUploading" class="upload-overlay">
          <div class="upload-spinner"></div>
          <span>上传中...</span>
        </div>
      </div>
      
      <!-- Preview pane -->
      <div 
        v-if="viewMode !== 'edit'" 
        class="preview-pane"
      >
        <div class="md-preview" v-html="renderedContent"></div>
      </div>
    </div>
    
    <div class="editor-hint">
      💡 支持 Markdown 语法 | 可拖拽或粘贴图片上传 | 实时预览
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
  align-items: center;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 4px;
}

.toolbar-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover:not(:disabled) {
  background: var(--color-primary);
  color: white;
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.view-mode-toggle {
  display: flex;
  gap: 4px;
  padding: 3px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 6px;
}

.mode-btn {
  padding: 6px 12px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.mode-btn:hover {
  color: var(--text-primary);
}

.mode-btn.active {
  background: var(--color-primary);
  color: white;
}

.upload-error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  font-size: 13px;
}

.upload-error button {
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  font-size: 18px;
  padding: 0 4px;
}

.editor-content {
  display: flex;
  min-height: 400px;
}

.editor-content.edit .edit-pane {
  width: 100%;
}

.editor-content.preview .preview-pane {
  width: 100%;
}

.editor-content.split .edit-pane,
.editor-content.split .preview-pane {
  width: 50%;
}

.edit-pane {
  position: relative;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.editor-content.edit .edit-pane {
  border-right: none;
}

.md-textarea {
  width: 100%;
  height: 100%;
  min-height: 400px;
  padding: 16px;
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
}

.md-textarea:focus {
  outline: none;
}

.md-textarea::placeholder {
  color: var(--text-muted);
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: white;
}

.upload-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.preview-pane {
  overflow-y: auto;
  background: rgba(0, 0, 0, 0.1);
}

.md-preview {
  padding: 16px;
  min-height: 400px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.md-preview :deep(.empty-hint) {
  color: var(--text-muted);
  font-style: italic;
}

.md-preview :deep(h1),
.md-preview :deep(h2),
.md-preview :deep(h3),
.md-preview :deep(h4),
.md-preview :deep(h5),
.md-preview :deep(h6) {
  color: var(--text-primary);
  margin: 20px 0 12px;
  font-weight: 600;
}

.md-preview :deep(h1) { font-size: 2rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 8px; }
.md-preview :deep(h2) { font-size: 1.5rem; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 6px; }
.md-preview :deep(h3) { font-size: 1.25rem; }
.md-preview :deep(h4) { font-size: 1.1rem; }

.md-preview :deep(p) {
  margin-bottom: 12px;
}

.md-preview :deep(ul),
.md-preview :deep(ol) {
  padding-left: 24px;
  margin-bottom: 12px;
}

.md-preview :deep(li) {
  margin-bottom: 6px;
}

.md-preview :deep(code) {
  background: rgba(0, 0, 0, 0.3);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 0.9em;
  color: #e06c75;
}

.md-preview :deep(pre) {
  background: rgba(0, 0, 0, 0.4);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.md-preview :deep(pre code) {
  background: none;
  padding: 0;
  color: #abb2bf;
}

.md-preview :deep(a) {
  color: var(--color-primary);
  text-decoration: underline;
}

.md-preview :deep(a:hover) {
  color: var(--color-secondary);
}

.md-preview :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  padding-left: 16px;
  margin: 16px 0;
  color: var(--text-muted);
  background: rgba(0, 0, 0, 0.1);
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
}

.md-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.md-preview :deep(th),
.md-preview :deep(td) {
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 10px 14px;
  text-align: left;
}

.md-preview :deep(th) {
  background: rgba(0, 0, 0, 0.2);
  font-weight: 600;
}

.md-preview :deep(tr:nth-child(even)) {
  background: rgba(0, 0, 0, 0.1);
}

.md-preview :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}

.md-preview :deep(hr) {
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  margin: 24px 0;
}

.md-preview :deep(input[type="checkbox"]) {
  margin-right: 8px;
}

.editor-hint {
  padding: 8px 12px;
  font-size: 12px;
  color: var(--text-muted);
  background: rgba(0, 0, 0, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

@media (max-width: 768px) {
  .editor-content.split {
    flex-direction: column;
  }
  
  .editor-content.split .edit-pane,
  .editor-content.split .preview-pane {
    width: 100%;
    min-height: 250px;
  }
  
  .editor-content.split .edit-pane {
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .md-textarea {
    min-height: 250px;
  }
  
  .md-preview {
    min-height: 250px;
  }
  
  .toolbar-buttons {
    order: 2;
    width: 100%;
  }
  
  .view-mode-toggle {
    order: 1;
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .toolbar-btn {
    min-width: 28px;
    height: 28px;
    font-size: 11px;
  }
  
  .mode-btn {
    font-size: 11px;
    padding: 4px 8px;
  }
  
  .toolbar-divider {
    display: none;
  }
}
</style>
