<template>
  <div class="fu-wrap">
    <el-upload
      ref="uploadRef"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="uploadFile"
      accept="image/*"
    >
      <template #trigger>
        <div v-if="modelValue" class="fu-preview">
          <img :src="modelValue" />
          <div class="fu-overlay">
            <span>点击替换</span>
          </div>
        </div>
        <div v-else class="fu-placeholder">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <path d="M21 15l-5-5L5 21"/>
          </svg>
          <span>点击上传封面图</span>
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import request from '../api/request'

const props = defineProps({ modelValue: String })
const emit = defineEmits(['update:modelValue'])

function beforeUpload(file) {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) { ElMessage.warning('图片不能超过10MB'); return false }
  return true
}

async function uploadFile({ file }) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    emit('update:modelValue', res.data)
    ElMessage.success('上传成功')
  } catch { /* handled by interceptor */ }
}
</script>

<style scoped>
.fu-wrap { width: 100%; }

.fu-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px 16px;
  border: 2px dashed var(--c-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--c-text-muted);
  font-size: 0.85rem;
  transition: var(--transition);
  background: var(--c-bg);
}
.fu-placeholder:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.fu-preview {
  position: relative;
  width: 100%;
  max-height: 200px;
  overflow: hidden;
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.fu-preview img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  display: block;
}
.fu-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 0.85rem;
  opacity: 0;
  transition: var(--transition);
}
.fu-preview:hover .fu-overlay { opacity: 1; }
</style>
