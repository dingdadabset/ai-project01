<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { MdEditor, config } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import uploadApi from '@/api/uploads'

// Configure md-editor-v3 global settings
config({
  editorConfig: {
    languageUserDefined: {
      'zh-CN': {
        toolbarTips: {
          bold: '加粗',
          underline: '下划线',
          italic: '斜体',
          strikeThrough: '删除线',
          title: '标题',
          sub: '下标',
          sup: '上标',
          quote: '引用',
          unorderedList: '无序列表',
          orderedList: '有序列表',
          task: '任务列表',
          codeRow: '行内代码',
          code: '代码块',
          link: '链接',
          image: '图片',
          table: '表格',
          mermaid: '流程图',
          katex: '公式',
          revoke: '撤销',
          next: '重做',
          save: '保存',
          prettier: '美化',
          pageFullscreen: '页面全屏',
          fullscreen: '全屏',
          preview: '预览',
          htmlPreview: 'HTML预览',
          catalog: '目录',
          github: 'GitHub'
        },
        titleItem: {
          h1: '一级标题',
          h2: '二级标题',
          h3: '三级标题',
          h4: '四级标题',
          h5: '五级标题',
          h6: '六级标题'
        },
        imgTitleItem: {
          link: '添加链接',
          upload: '上传图片',
          clip2upload: '粘贴上传'
        },
        linkModalTips: {
          linkTitle: '链接描述',
          imageTitle: '图片描述',
          descLabel: '描述文本',
          descLabelPlaceHolder: '请输入描述',
          urlLabel: '链接地址',
          urlLabelPlaceHolder: '请输入链接',
          buttonOK: '确定'
        },
        clipModalTips: {
          title: '粘贴图片上传',
          buttonUpload: '上传'
        },
        copyCode: {
          text: '复制代码',
          successTips: '复制成功！',
          failTips: '复制失败！'
        },
        mermaid: {
          flow: '流程图',
          sequence: '时序图',
          gantt: '甘特图',
          class: '类图',
          state: '状态图',
          pie: '饼图',
          relationship: 'ER图',
          journey: '用户旅程图'
        },
        katex: {
          inline: '行内公式',
          block: '块级公式'
        },
        footer: {
          markdownTotal: '字数',
          scrollAuto: '同步滚动'
        }
      }
    }
  }
})

const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
}>(), {
  placeholder: '请输入Markdown内容...\n\n支持拖放或粘贴图片上传'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorId = ref('md-editor-' + Math.random().toString(36).substring(2, 11))

const uploadError = ref('')
const wrapperRef = ref<HTMLElement | null>(null)
const isFullscreen = ref(false)

const content = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value)
})

// Watch for fullscreen class changes on the editor
const checkFullscreen = () => {
  if (wrapperRef.value) {
    const editor = wrapperRef.value.querySelector('.md-editor')
    if (editor) {
      const hasFullscreenClass = editor.classList.contains('md-editor-fullscreen')
      isFullscreen.value = hasFullscreenClass
      
      // Lock body scroll when in fullscreen mode
      if (hasFullscreenClass) {
        document.body.style.overflow = 'hidden'
        document.body.classList.add('editor-fullscreen-active')
      } else {
        document.body.style.overflow = ''
        document.body.classList.remove('editor-fullscreen-active')
      }
    }
  }
}

// Set up mutation observer to detect fullscreen class changes
let observer: MutationObserver | null = null

onMounted(() => {
  if (wrapperRef.value) {
    observer = new MutationObserver(checkFullscreen)
    const editor = wrapperRef.value.querySelector('.md-editor')
    if (editor) {
      observer.observe(editor, { attributes: true, attributeFilter: ['class'] })
    }
  }
})

// Cleanup on unmount
onUnmounted(() => {
  // Disconnect the observer
  if (observer) {
    observer.disconnect()
    observer = null
  }
  
  // Reset body state in case we were in fullscreen
  document.body.style.overflow = ''
  document.body.classList.remove('editor-fullscreen-active')
})

// Toggle fullscreen mode programmatically
const toggleFullscreen = () => {
  if (wrapperRef.value) {
    const editor = wrapperRef.value.querySelector('.md-editor')
    if (editor) {
      // Find and click the pageFullscreen button in the toolbar
      const fullscreenBtn = editor.querySelector('[title="页面全屏"]') || 
                            editor.querySelector('[title="取消全屏"]') ||
                            editor.querySelector('.md-editor-toolbar-item[title*="全屏"]')
      if (fullscreenBtn) {
        (fullscreenBtn as HTMLElement).click()
      } else {
        // Manually toggle fullscreen class if button not found
        editor.classList.toggle('md-editor-fullscreen')
        checkFullscreen()
      }
    }
  }
}

// Handle image upload
const onUploadImg = async (files: File[], callback: (urls: string[]) => void) => {
  const uploadedUrls: string[] = []
  uploadError.value = ''
  
  for (const file of files) {
    try {
      // Upload to markdown-images directory
      const response = await uploadApi.upload(file, 'markdown-images')
      if (response.data.success && response.data.url) {
        uploadedUrls.push(response.data.url)
      } else {
        const errorMsg = response.data.error || '图片上传失败'
        uploadError.value = errorMsg
        console.error('Upload failed:', errorMsg)
      }
    } catch (error) {
      uploadError.value = '图片上传失败，请重试'
      console.error('Upload error:', error)
    }
  }
  
  callback(uploadedUrls)
}
</script>

<template>
  <div ref="wrapperRef" class="markdown-editor-wrapper" :class="{ 'wrapper-fullscreen': isFullscreen }">
    <!-- Prominent fullscreen toggle button -->
    <div class="editor-header">
      <button 
        class="fullscreen-toggle-btn" 
        @click="toggleFullscreen"
        :title="isFullscreen ? '退出全屏' : '全屏编辑'"
      >
        <span v-if="!isFullscreen">🖥️ 全屏编辑</span>
        <span v-else>❌ 退出全屏</span>
      </button>
    </div>
    <!-- Upload error message -->
    <div v-if="uploadError" class="upload-error-banner">
      {{ uploadError }}
      <button @click="uploadError = ''">&times;</button>
    </div>
    <MdEditor
      :editorId="editorId"
      v-model="content"
      :placeholder="placeholder"
      language="zh-CN"
      theme="dark"
      previewTheme="github"
      codeTheme="atom"
      :showCodeRowNumber="true"
      :autoFocus="false"
      :tableShape="[8, 6]"
      :footers="['markdownTotal', 'scrollSwitch']"
      :toolbars="[
        'bold',
        'underline',
        'italic',
        'strikeThrough',
        '-',
        'title',
        'sub',
        'sup',
        '-',
        'quote',
        'unorderedList',
        'orderedList',
        'task',
        '-',
        'codeRow',
        'code',
        'link',
        'image',
        'table',
        '-',
        'revoke',
        'next',
        '=',
        'preview',
        'pageFullscreen',
        'fullscreen'
      ]"
      @onUploadImg="onUploadImg"
    />
  </div>
</template>

<style>
/* Z-index hierarchy for fullscreen mode */
:root {
  --z-editor-fullscreen: 99999;
  --z-editor-fullscreen-toolbar: 100000;
}

/* Dark theme adjustments for md-editor-v3 */
.markdown-editor-wrapper {
  border-radius: var(--radius-md, 16px);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* Editor header with prominent fullscreen button */
.editor-header {
  display: flex;
  justify-content: flex-end;
  padding: 8px 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.fullscreen-toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.fullscreen-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.fullscreen-toggle-btn:active {
  transform: translateY(0);
}

/* Fullscreen mode header styling */
.wrapper-fullscreen .editor-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100001;
  background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
}

/* Upload error banner */
.upload-error-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  font-size: 14px;
  border-bottom: 1px solid rgba(239, 68, 68, 0.3);
}

.upload-error-banner button {
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  font-size: 18px;
  padding: 0 4px;
  line-height: 1;
}

.upload-error-banner button:hover {
  color: #f87171;
}

.markdown-editor-wrapper .md-editor {
  --md-bk-color: var(--bg-secondary, #1a1a2e) !important;
  --md-border-color: rgba(255, 255, 255, 0.1) !important;
  --md-color: var(--text-primary, #ffffff) !important;
}

.markdown-editor-wrapper .md-editor-dark {
  --md-bk-color: var(--bg-secondary, #1a1a2e) !important;
  --md-border-color: rgba(255, 255, 255, 0.1) !important;
  --md-color: var(--text-primary, #ffffff) !important;
}

/* Toolbar styling */
.markdown-editor-wrapper .md-editor .md-editor-toolbar {
  background: rgba(0, 0, 0, 0.2) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.markdown-editor-wrapper .md-editor .md-editor-toolbar-wrapper .md-editor-toolbar-item {
  color: var(--text-secondary, #a1a1aa) !important;
}

.markdown-editor-wrapper .md-editor .md-editor-toolbar-wrapper .md-editor-toolbar-item:hover {
  background: var(--color-primary, #6366f1) !important;
  color: white !important;
}

/* Content area styling */
.markdown-editor-wrapper .md-editor .md-editor-content {
  background: var(--bg-secondary, #1a1a2e) !important;
}

.markdown-editor-wrapper .md-editor .md-editor-input-wrapper {
  background: transparent !important;
}

.markdown-editor-wrapper .md-editor .md-editor-preview-wrapper {
  background: rgba(0, 0, 0, 0.1) !important;
}

/* CodeMirror editor styling */
.markdown-editor-wrapper .md-editor .cm-editor {
  background: transparent !important;
}

.markdown-editor-wrapper .md-editor .cm-editor .cm-content {
  color: var(--text-primary, #ffffff) !important;
}

.markdown-editor-wrapper .md-editor .cm-editor .cm-gutters {
  background: rgba(0, 0, 0, 0.2) !important;
  color: var(--text-muted, #71717a) !important;
  border-right: 1px solid rgba(255, 255, 255, 0.1) !important;
}

/* Footer styling */
.markdown-editor-wrapper .md-editor .md-editor-footer {
  background: rgba(0, 0, 0, 0.1) !important;
  border-top: 1px solid rgba(255, 255, 255, 0.05) !important;
  color: var(--text-muted, #71717a) !important;
}

/* Modal styling */
.markdown-editor-wrapper .md-editor .md-editor-modal {
  background: var(--bg-card, #16213e) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.markdown-editor-wrapper .md-editor .md-editor-modal input,
.markdown-editor-wrapper .md-editor .md-editor-modal textarea {
  background: var(--bg-secondary, #1a1a2e) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  color: var(--text-primary, #ffffff) !important;
}

.markdown-editor-wrapper .md-editor .md-editor-modal button {
  background: var(--color-primary, #6366f1) !important;
  color: white !important;
  border: none !important;
}

/* Dropdown menu styling */
.markdown-editor-wrapper .md-editor .md-editor-dropdown {
  background: var(--bg-card, #16213e) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.markdown-editor-wrapper .md-editor .md-editor-dropdown li:hover {
  background: var(--color-primary, #6366f1) !important;
  color: white !important;
}

/* Set minimum height */
.markdown-editor-wrapper .md-editor {
  min-height: 450px;
}

/* Fullscreen mode support - Page Fullscreen */
.markdown-editor-wrapper .md-editor.md-editor-fullscreen {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: var(--z-editor-fullscreen) !important;
  border-radius: 0 !important;
  margin: 0 !important;
  max-width: 100vw !important;
  max-height: 100vh !important;
}

/* Handle the wrapper when editor is in fullscreen - JavaScript-based class for better browser compatibility */
.markdown-editor-wrapper.wrapper-fullscreen {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  z-index: var(--z-editor-fullscreen) !important;
  border-radius: 0 !important;
  border: none !important;
  overflow: visible !important;
  max-width: 100vw !important;
  max-height: 100vh !important;
}

/* Fallback: Also support :has() for modern browsers */
@supports selector(:has(*)) {
  .markdown-editor-wrapper:has(.md-editor-fullscreen) {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    z-index: var(--z-editor-fullscreen) !important;
    border-radius: 0 !important;
    border: none !important;
    overflow: visible !important;
    max-width: 100vw !important;
    max-height: 100vh !important;
  }
}

/* Global body style when editor is in fullscreen mode */
body.editor-fullscreen-active {
  overflow: hidden !important;
}

/* Hide modal overlay when editor is in fullscreen */
body.editor-fullscreen-active .modal-overlay {
  overflow: visible !important;
}

body.editor-fullscreen-active .modal-content {
  overflow: visible !important;
  max-height: none !important;
}

/* Fullscreen toolbar stays on top */
.md-editor.md-editor-fullscreen .md-editor-toolbar {
  position: sticky !important;
  top: 0 !important;
  z-index: var(--z-editor-fullscreen-toolbar) !important;
}

/* Fullscreen content area fills the rest */
.md-editor.md-editor-fullscreen .md-editor-content {
  height: calc(100vh - 50px) !important;
}

/* Browser Fullscreen API mode */
.md-editor:-webkit-full-screen,
.md-editor:-moz-full-screen,
.md-editor:-ms-fullscreen,
.md-editor:fullscreen {
  width: 100% !important;
  height: 100% !important;
}
</style>
