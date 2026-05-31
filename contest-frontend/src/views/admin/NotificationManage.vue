<template>
  <div class="nm-page">
    <div class="nm-bg">
      <div class="container">
        <div class="nm-header">
          <div>
            <h1 class="nm-title">通知推送</h1>
            <p class="nm-subtitle">发送通知给指定用户或全体广播</p>
          </div>
        </div>

        <div class="nm-grid">
          <div class="nm-card">
            <h3 class="nm-card-title">发送给指定用户</h3>
            <el-form :model="form" label-position="top" class="nm-form">
              <el-form-item label="用户ID" required>
                <el-input v-model="form.userId" placeholder="输入用户ID" type="number" />
              </el-form-item>
              <el-form-item label="标题" required>
                <el-input v-model="form.title" placeholder="通知标题" />
              </el-form-item>
              <el-form-item label="内容" required>
                <el-input v-model="form.content" type="textarea" :rows="4" placeholder="通知内容" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="form.type">
                  <el-option :value="0" label="审核结果" />
                  <el-option :value="1" label="团队申请" />
                  <el-option :value="2" label="团队结果" />
                  <el-option :value="3" label="竞赛变更" />
                  <el-option :value="4" label="系统通知" />
                </el-select>
              </el-form-item>
              <el-button class="nm-btn" @click="handleSend" :loading="sending">发送</el-button>
            </el-form>
          </div>

          <div class="nm-card">
            <h3 class="nm-card-title">发送广播（全体用户）</h3>
            <el-form :model="broadcastForm" label-position="top" class="nm-form">
              <el-form-item label="标题" required>
                <el-input v-model="broadcastForm.title" placeholder="通知标题" />
              </el-form-item>
              <el-form-item label="内容" required>
                <el-input v-model="broadcastForm.content" type="textarea" :rows="4" placeholder="通知内容" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="broadcastForm.type">
                  <el-option :value="0" label="审核结果" />
                  <el-option :value="1" label="团队申请" />
                  <el-option :value="2" label="团队结果" />
                  <el-option :value="3" label="竞赛变更" />
                  <el-option :value="4" label="系统通知" />
                </el-select>
              </el-form-item>
              <el-button class="nm-btn nm-btn--accent" @click="handleBroadcast" :loading="broadcasting">群发广播</el-button>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { sendNotification, sendBroadcast } from '../../api/notification'

const sending = ref(false)
const broadcasting = ref(false)

const form = reactive({ userId: '', title: '', content: '', type: 4 })
const broadcastForm = reactive({ title: '', content: '', type: 4 })

async function handleSend() {
  if (!form.userId || !form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  sending.value = true
  try {
    await sendNotification({ userId: form.userId, type: form.type, title: form.title, content: form.content })
    ElMessage.success('发送成功')
    form.userId = ''; form.title = ''; form.content = ''
  } finally { sending.value = false }
}

async function handleBroadcast() {
  if (!broadcastForm.title || !broadcastForm.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  broadcasting.value = true
  try {
    await sendBroadcast({ type: broadcastForm.type, title: broadcastForm.title, content: broadcastForm.content })
    ElMessage.success('广播发送成功')
    broadcastForm.title = ''; broadcastForm.content = ''
  } finally { broadcasting.value = false }
}
</script>

<style scoped>
.nm-page { background: #f5f3ef; font-family: 'DM Sans', sans-serif; }
.nm-bg { padding: 40px 0 60px; }
.container { max-width: 860px; margin: 0 auto; padding: 0 24px; }
.nm-header { margin-bottom: 32px; }
.nm-title { font-family: 'DM Serif Display', serif; font-size: 32px; color: #1a2332; margin: 0 0 6px; }
.nm-subtitle { font-size: 14px; color: #1a2332; opacity: 0.5; margin: 0; }
.nm-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
@media (max-width: 768px) { .nm-grid { grid-template-columns: 1fr; } }
.nm-card { background: #fff; border-radius: 16px; padding: 28px; box-shadow: 0 1px 3px rgba(26,35,50,0.06); }
.nm-card-title { font-family: 'DM Serif Display', serif; font-size: 18px; color: #1a2332; margin: 0 0 24px; padding-bottom: 16px; border-bottom: 1px solid #f0eeea; }
.nm-form :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: #1a2332; padding-bottom: 4px; }
.nm-form :deep(.el-input__wrapper), .nm-form :deep(.el-textarea__inner) { border-radius: 10px; border: 1px solid #e0ddd7; box-shadow: none; }
.nm-form :deep(.el-input__wrapper:hover), .nm-form :deep(.el-textarea__inner:hover) { border-color: #1a2332; }
.nm-form :deep(.el-input__wrapper.is-focus) { border-color: #1a2332; box-shadow: 0 0 0 2px rgba(26,35,50,0.08); }
.nm-btn { width: 100%; height: 40px; border: none; border-radius: 10px; background: #1a2332; color: #fff; font-family: 'DM Sans', sans-serif; font-size: 14px; font-weight: 500; cursor: pointer; margin-top: 8px; }
.nm-btn:hover { background: #2a3a52; }
.nm-btn--accent { background: #e85d4a; }
.nm-btn--accent:hover { background: #d04a38; }
</style>
