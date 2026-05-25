<template>
  <div class="cms-page">
    <NavBar />
    <div class="cms-bg">
      <div class="container">
        <div class="cms-header">
          <div>
            <h1 class="cms-title">内容管理</h1>
            <p class="cms-subtitle">管理轮播图与公告内容</p>
          </div>
        </div>

        <div class="cms-card">
          <el-tabs v-model="activeTab" class="cms-tabs">
            <el-tab-pane label="轮播图" name="banner">
              <div class="cms-tab-header">
                <span class="cms-tab-count">{{ banners.length }} 项</span>
                <button class="cms-add-btn" @click="showAdd('banner')">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="margin-right:6px"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
                  添加轮播图
                </button>
              </div>
              <el-table :data="banners" v-loading="false" element-loading-background="rgba(245,243,239,0.8)">
                <el-table-column prop="id" label="ID" width="64" />
                <el-table-column prop="title" label="描述" min-width="140" />
                <el-table-column prop="imageUrl" label="图片" width="200">
                  <template #default="{ row }">
                    <img :src="row.imageUrl" class="cms-thumb" />
                  </template>
                </el-table-column>
                <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
                <el-table-column prop="linkUrl" label="跳转链接" min-width="160" />
                <el-table-column label="操作" width="160" align="right">
                  <template #default="{ row }">
                    <el-button class="cms-action-btn" @click="editItem(row)">编辑</el-button>
                    <el-button class="cms-action-btn cms-action--danger" @click="del(row.id)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="公告" name="announcement">
              <div class="cms-tab-header">
                <span class="cms-tab-count">{{ announcements.length }} 项</span>
                <button class="cms-add-btn" @click="showAdd('announcement')">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="margin-right:6px"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
                  添加公告
                </button>
              </div>
              <el-table :data="announcements" v-loading="false" element-loading-background="rgba(245,243,239,0.8)">
                <el-table-column prop="id" label="ID" width="64" />
                <el-table-column prop="title" label="标题" min-width="140" />
                <el-table-column prop="position" label="位置" width="150" />
                <el-table-column prop="status" label="状态" width="80" align="center">
                  <template #default="{ row }">
                    <span :class="['cms-status', row.status === 1 ? 'cms-status--active' : 'cms-status--hidden']">{{ row.status === 1 ? '展示' : '隐藏' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="publishTime" label="发布时间" width="180" />
                <el-table-column label="操作" width="160" align="right">
                  <template #default="{ row }">
                    <el-button class="cms-action-btn" @click="editItem(row)">编辑</el-button>
                    <el-button class="cms-action-btn cms-action--danger" @click="del(row.id)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑' : '添加'"
      width="580px"
      :close-on-click-modal="false"
      class="cms-dialog"
    >
      <el-form :model="form" label-width="100px" class="cms-form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="输入标题" />
        </el-form-item>
        <el-form-item v-if="activeTab === 'announcement'" label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="输入公告内容" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="输入图片链接" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="输入跳转链接（可选）" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="activeTab === 'announcement'" label="展示位置">
              <el-select v-model="form.position" class="cms-select">
                <el-option label="首页滚动" value="home_scroll" />
                <el-option label="消息中心" value="message_center" />
                <el-option label="弹窗" value="popup" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="定时发布">
          <el-date-picker v-model="form.publishTime" type="datetime" placeholder="选择发布时间" clearable class="cms-datepicker" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="cms-dialog-cancel" @click="dialogVisible = false">取消</el-button>
        <el-button class="cms-dialog-save" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { listBanners, listAnnouncements, createCmsContent, updateCmsContent, deleteCmsContent } from '../../api/cms'

const activeTab = ref('banner')
const banners = ref([])
const announcements = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ sortOrder: 0, status: 1 })

async function loadBanners() {
  try {
    const res = await listBanners()
    banners.value = res.data || []
  } catch (e) { /* ignore */ }
}

async function loadAnnouncements() {
  try {
    const res = await listAnnouncements()
    announcements.value = res.data || []
  } catch (e) { /* ignore */ }
}

function showAdd(type) {
  isEdit.value = false
  Object.assign(form, { title: '', content: '', imageUrl: '', linkUrl: '', sortOrder: 0, position: 'home_scroll', publishTime: null })
  dialogVisible.value = true
}

function editItem(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  const payload = { ...form, contentType: activeTab.value === 'banner' ? 0 : 1 }
  if (isEdit.value) {
    await updateCmsContent(payload)
  } else {
    await createCmsContent(payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  refresh()
}

async function del(id) {
  await ElMessageBox.confirm('确定删除？')
  await deleteCmsContent(id)
  ElMessage.success('已删除')
  refresh()
}

function refresh() {
  if (activeTab.value === 'banner') loadBanners()
  else loadAnnouncements()
}

watch(activeTab, () => refresh())

onMounted(() => { loadBanners(); loadAnnouncements() })
</script>

<style scoped>
.cms-page {
  min-height: 100vh;
  padding-top: 72px;
  background: #f5f3ef;
  font-family: 'DM Sans', sans-serif;
}

.cms-bg {
  padding: 40px 0 60px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
}

.cms-header {
  margin-bottom: 32px;
}

.cms-title {
  font-family: 'DM Serif Display', serif;
  font-size: 32px;
  color: #1a2332;
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.cms-subtitle {
  font-size: 14px;
  color: #1a2332;
  opacity: 0.5;
  margin: 0;
}

.cms-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(26, 35, 50, 0.06);
}

.cms-tabs :deep(.el-tabs__header) {
  margin: 0 0 24px;
  border-bottom: 1px solid #f0eeea;
}

.cms-tabs :deep(.el-tabs__item) {
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #1a2332;
  opacity: 0.5;
  height: 44px;
  line-height: 44px;
  padding: 0 20px;
  transition: all 0.2s ease;
}

.cms-tabs :deep(.el-tabs__item.is-active) {
  opacity: 1;
  color: #1a2332;
}

.cms-tabs :deep(.el-tabs__active-bar) {
  background: #1a2332;
  height: 2px;
}

.cms-tab-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.cms-tab-count {
  font-size: 13px;
  color: #1a2332;
  opacity: 0.4;
  font-weight: 500;
}

.cms-add-btn {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 18px;
  background: #1a2332;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-family: 'DM Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cms-add-btn:hover {
  background: #2a3a52;
}

.cms-card :deep(.el-table) {
  border: none;
}

.cms-card :deep(.el-table th.el-table__cell) {
  background: #f5f3ef;
  color: #1a2332;
  font-family: 'DM Sans', sans-serif;
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  border-bottom: none;
}

.cms-card :deep(.el-table th.el-table__cell > .cell) {
  padding: 12px 16px;
}

.cms-card :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f0eeea;
  padding: 10px 0;
}

.cms-card :deep(.el-table::before) {
  display: none;
}

.cms-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #faf9f7;
}

.cms-thumb {
  width: 120px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0eeea;
}

.cms-status {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.cms-status--active {
  background: rgba(26, 122, 90, 0.1);
  color: #1a7a5a;
}

.cms-status--hidden {
  background: rgba(26, 35, 50, 0.06);
  color: #1a2332;
  opacity: 0.5;
}

.cms-action-btn {
  height: 30px;
  padding: 0 14px;
  border: 1px solid #e0ddd7;
  border-radius: 8px;
  background: #fff;
  color: #1a2332;
  font-family: 'DM Sans', sans-serif;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cms-action-btn:hover {
  border-color: #1a2332;
  background: #1a2332;
  color: #fff;
}

.cms-action--danger {
  color: #e85d4a;
  border-color: rgba(232, 93, 74, 0.3);
}

.cms-action--danger:hover {
  background: #e85d4a;
  border-color: #e85d4a;
  color: #fff;
}

.cms-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(26, 35, 50, 0.15);
}

.cms-dialog :deep(.el-dialog__header) {
  background: #f5f3ef;
  padding: 24px 28px 16px;
  margin: 0;
}

.cms-dialog :deep(.el-dialog__title) {
  font-family: 'DM Serif Display', serif;
  font-size: 20px;
  color: #1a2332;
}

.cms-dialog :deep(.el-dialog__body) {
  padding: 28px;
}

.cms-dialog :deep(.el-dialog__footer) {
  padding: 0 28px 24px;
  border-top: none;
}

.cms-form :deep(.el-form-item__label) {
  font-family: 'DM Sans', sans-serif;
  font-weight: 500;
  color: #1a2332;
}

.cms-form :deep(.el-input__wrapper),
.cms-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #e0ddd7;
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.cms-form :deep(.el-input__wrapper:hover),
.cms-form :deep(.el-textarea__inner:hover) {
  border-color: #1a2332;
}

.cms-form :deep(.el-input__wrapper.is-focus) {
  border-color: #1a2332;
  box-shadow: 0 0 0 2px rgba(26, 35, 50, 0.08);
}

.cms-select {
  width: 100%;
}

.cms-select :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #e0ddd7;
  box-shadow: none;
}

.cms-select :deep(.el-input__wrapper:hover),
.cms-select :deep(.el-input__wrapper.is-focus) {
  border-color: #1a2332;
  box-shadow: 0 0 0 2px rgba(26, 35, 50, 0.08);
}

.cms-datepicker {
  width: 100%;
}

.cms-dialog-cancel {
  height: 38px;
  padding: 0 22px;
  border: 1px solid #e0ddd7;
  border-radius: 10px;
  background: #fff;
  color: #1a2332;
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cms-dialog-cancel:hover {
  border-color: #1a2332;
}

.cms-dialog-save {
  height: 38px;
  padding: 0 22px;
  border: none;
  border-radius: 10px;
  background: #1a2332;
  color: #fff;
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cms-dialog-save:hover {
  background: #2a3a52;
}

@media (max-width: 768px) {
  .cms-card {
    padding: 20px;
    border-radius: 12px;
  }
}
</style>
