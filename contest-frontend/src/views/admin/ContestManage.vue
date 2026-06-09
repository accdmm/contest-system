<template>
  <div class="cm-page">
    <div class="cm-bg">
      <div class="container">
        <div class="cm-header">
          <div>
            <h1 class="cm-title">竞赛管理</h1>
            <p class="cm-subtitle">发布、编辑与管理所有竞赛项目</p>
          </div>
          <el-button v-if="store.hasPerm('contest:create')" class="cm-add-btn" @click="openCreate">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="margin-right:6px"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
            新建竞赛
          </el-button>
        </div>

        <div class="cm-table-wrap">
          <el-table :data="list" v-loading="loading" element-loading-background="rgba(245,243,239,0.8)" empty-text="暂无数据">
            <el-table-column prop="id" label="ID" width="64" />
            <el-table-column prop="name" label="名称" min-width="160">
              <template #default="{ row }">
                <span class="cm-cell-name">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="类别" width="100" />
            <el-table-column prop="level" label="级别" width="80" />
            <el-table-column prop="creatorName" label="创建人" width="100" />
            <el-table-column prop="currentCount" label="报名数" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <span :class="['cm-status', `cm-status--${statusType(row.status)}`]">{{ statusLabel(row.status) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="290" fixed="right" align="right">
              <template #default="{ row }">
                <el-button v-if="store.hasPerm('contest:update')" class="cm-action-btn" @click="edit(row)">编辑</el-button>
                <el-button v-if="store.hasPerm('contest:publish') && row.status === 0" class="cm-action-btn cm-action--success" @click="publish(row.id)">上架</el-button>
                <el-button v-if="store.hasPerm('contest:publish') && row.status === 1" class="cm-action-btn cm-action--warning" @click="unpublish(row.id)">下架</el-button>
                <el-button v-if="store.hasPerm('contest:delete') && row.status === 0" class="cm-action-btn cm-action--danger" @click="del(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-if="total > 0"
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            @current-change="pageChange"
            class="cm-pagination"
          />
        </div>
      </div>
    </div>

        <el-dialog
          v-model="dialogVisible"
          :title="isEdit ? '编辑竞赛' : '新建竞赛'"
          width="680px"
          :close-on-click-modal="false"
          destroy-on-close
          class="cm-dialog"
        >
      <el-form :model="form" label-width="100px" class="cm-form">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="输入竞赛名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="类别" required>
              <el-select v-model="form.category" placeholder="选择类别">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="级别" required>
              <el-select v-model="form.level" placeholder="选择级别">
                <el-option label="校级" value="校级" />
                <el-option label="省级" value="省级" />
                <el-option label="国家级" value="国家级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="主办方">
              <el-input v-model="form.organizer" placeholder="输入主办单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点">
              <el-input v-model="form.location" placeholder="输入竞赛地点" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="竞赛时间">
          <el-date-picker v-model="form.contestTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择竞赛时间" class="cm-datepicker" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="报名开始">
              <el-date-picker v-model="form.registerStartTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" class="cm-datepicker" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名截止">
              <el-date-picker v-model="form.registerEndTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择截止时间" class="cm-datepicker" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参赛形式" required>
          <div class="cm-radio-group">
            <label :class="['cm-radio-option', { active: form.contestType === 0 }]" @click="form.contestType = 0">
              <span class="cm-radio-dot" />
              仅个人赛
            </label>
            <label :class="['cm-radio-option', { active: form.contestType === 1 }]" @click="form.contestType = 1">
              <span class="cm-radio-dot" />
              仅团队赛
            </label>
            <label :class="['cm-radio-option', { active: form.contestType === 2 }]" @click="form.contestType = 2">
              <span class="cm-radio-dot" />
              两者皆可
            </label>
          </div>
        </el-form-item>
        <el-row v-if="form.contestType !== 0" :gutter="16">
          <el-col :span="12">
            <el-form-item label="团队最少人数">
              <el-input-number v-model="form.teamMinSize" :min="1" :max="form.teamMaxSize" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="团队最多人数">
              <el-input-number v-model="form.teamMaxSize" :min="form.teamMinSize" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图">
          <FileUpload v-model="form.coverImageUrl" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="输入竞赛描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button class="cm-dialog-cancel" @click="dialogVisible = false">取消</el-button>
        <el-button class="cm-dialog-save" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FileUpload from '../../components/FileUpload.vue'
import { useUserStore } from '../../stores/user'
import { pageAdminContests, createContest, updateContest, publishContest, unpublishContest, deleteContest } from '../../api/contest'

const store = useUserStore()

const categories = ['理工类', '文史类', '艺术类', '体育类', '创新创业类']
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(true)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const initialForm = () => ({
  id: undefined,
  name: '', category: '', level: '', organizer: '', location: '',
  contestTime: undefined, registerStartTime: undefined, registerEndTime: undefined,
  contestType: 0, teamMinSize: 1, teamMaxSize: 10,
  coverImageUrl: '', description: ''
})

const form = reactive(initialForm())

const statusMap = { 0: '未发布', 1: '报名中', 2: '已截止' }
const statusTypeMap = { 0: 'info', 1: 'success', 2: 'warning' }
const statusLabel = s => statusMap[s] || ''
const statusType = s => statusTypeMap[s] || 'info'

function resetForm() {
  Object.assign(form, initialForm())
}

function openCreate() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

async function fetchData() {
  loading.value = true
  try {
    const res = await pageAdminContests({ page: page.value, size })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { ElMessage.error('加载竞赛列表失败') } finally { loading.value = false }
}

function edit(row) {
  const allowed = ['id', 'name', 'category', 'level', 'organizer', 'contestTime',
    'registerStartTime', 'registerEndTime', 'location', 'description',
    'contestType', 'teamMinSize', 'teamMaxSize', 'maxParticipants', 'coverImageUrl', 'attachmentUrls']
  const sanitized = { teamMinSize: row.teamMinSize || 1, teamMaxSize: row.teamMaxSize || 10 }
  for (const key of allowed) {
    if (key in row) sanitized[key] = row[key]
  }
  if (sanitized.contestTime == null) sanitized.contestTime = undefined
  if (sanitized.registerStartTime == null) sanitized.registerStartTime = undefined
  if (sanitized.registerEndTime == null) sanitized.registerEndTime = undefined
  Object.assign(form, sanitized)
  isEdit.value = true
  dialogVisible.value = true
}

async function save() {
  if (!form.name) { ElMessage.warning('请输入竞赛名称'); return }
  if (!form.category) { ElMessage.warning('请选择竞赛类别'); return }
  if (!form.level) { ElMessage.warning('请选择竞赛级别'); return }
  if (form.contestType !== 0 && form.teamMinSize > form.teamMaxSize) {
    ElMessage.warning('团队最少人数不能大于最多人数'); return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateContest(form)
    } else {
      await createContest(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // error handled by axios interceptor
  } finally { saving.value = false }
}

async function publish(id) { await publishContest(id); ElMessage.success('已上架'); fetchData() }
async function unpublish(id) { await unpublishContest(id); ElMessage.success('已下架'); fetchData() }

async function del(id) {
  try {
    await ElMessageBox.confirm('确定删除该竞赛吗？此操作不可撤销。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteContest(id)
    ElMessage.success('已删除')
    fetchData()
  } catch {
  }
}

function pageChange(p) { page.value = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.cm-page {
	background: var(--c-bg);
	font-family: 'DM Sans', sans-serif;
}

.cm-bg {
  padding: 40px 0 60px;
}

.cm-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  gap: 16px;
}

.cm-title {
  font-family: 'DM Serif Display', serif;
  font-size: 32px;
  color: var(--c-text);
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.cm-subtitle {
  font-size: 14px;
  color: var(--c-text);
  opacity: 0.5;
  margin: 0;
}

.cm-add-btn {
  display: inline-flex;
  align-items: center;
  height: 40px;
  padding: 0 20px;
  background: var(--c-primary);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.cm-add-btn:hover {
  background: var(--c-primary-light);
  color: #fff;
}

.cm-table-wrap {
  background: var(--c-surface);
  border-radius: var(--radius-md);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.cm-table-wrap :deep(.el-table) {
  border: none;
}

.cm-table-wrap :deep(.el-table th.el-table__cell) {
  background: var(--c-bg);
  color: var(--c-primary);
  font-family: 'DM Sans', sans-serif;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  border-bottom: none;
}

.cm-table-wrap :deep(.el-table th.el-table__cell > .cell) {
  padding: 12px 16px;
}

.cm-table-wrap :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid var(--c-border-light);
  padding: 10px 0;
}

.cm-table-wrap :deep(.el-table::before) {
  display: none;
}

.cm-table-wrap :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #faf9f7;
}

.cm-cell-name {
  font-weight: 500;
  color: var(--c-primary);
}

.cm-status {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.01em;
}

.cm-status--info {
  background: rgba(26, 35, 50, 0.06);
  color: var(--c-primary);
}

.cm-status--success {
  background: rgba(26, 122, 90, 0.1);
  color: #1a7a5a;
}

.cm-status--warning {
  background: rgba(230, 162, 60, 0.12);
  color: #c48920;
}

.cm-action-btn {
  height: 30px;
  padding: 0 14px;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: var(--c-surface);
  color: var(--c-primary);
  font-family: 'DM Sans', sans-serif;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cm-action-btn:hover {
  border-color: var(--c-primary);
  background: var(--c-primary);
  color: #fff;
}

.cm-action--success {
  color: #1a7a5a;
  border-color: rgba(26, 122, 90, 0.3);
}

.cm-action--success:hover {
  background: #1a7a5a;
  border-color: #1a7a5a;
  color: #fff;
}

.cm-action--warning {
  color: #c48920;
  border-color: rgba(196, 137, 32, 0.3);
}

.cm-action--warning:hover {
  background: #c48920;
  border-color: #c48920;
  color: #fff;
}

.cm-action--danger {
  color: var(--c-accent);
  border-color: rgba(232, 93, 74, 0.3);
}

.cm-action--danger:hover {
  background: var(--c-accent);
  border-color: var(--c-accent);
  color: #fff;
}

.cm-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.cm-pagination :deep(.el-pagination.is-background .btn-prev),
.cm-pagination :deep(.el-pagination.is-background .btn-next),
.cm-pagination :deep(.el-pagination.is-background .el-pager li) {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: 8px;
  margin: 0 2px;
  color: var(--c-primary);
  font-family: 'DM Sans', sans-serif;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.cm-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--c-primary);
  border-color: var(--c-primary);
  color: #fff;
}

.cm-dialog :deep(.el-dialog) {
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

.cm-dialog :deep(.el-dialog__header) {
  background: var(--c-bg);
  padding: 24px 28px 16px;
  margin: 0;
}

.cm-dialog :deep(.el-dialog__title) {
  font-family: 'DM Serif Display', serif;
  font-size: 20px;
  color: var(--c-primary);
}

.cm-dialog :deep(.el-dialog__body) {
  padding: 28px;
}

.cm-dialog :deep(.el-dialog__footer) {
  padding: 0 28px 24px;
  border-top: none;
}

.cm-form :deep(.el-form-item__label) {
  font-family: 'DM Sans', sans-serif;
  font-weight: 500;
  color: var(--c-primary);
}

.cm-form :deep(.el-input__wrapper),
.cm-form :deep(.el-select .el-input__wrapper),
.cm-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid var(--c-border);
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.cm-form :deep(.el-input__wrapper:hover),
.cm-form :deep(.el-select .el-input__wrapper:hover),
.cm-form :deep(.el-textarea__inner:hover) {
  border-color: var(--c-primary);
}

.cm-form :deep(.el-input__wrapper.is-focus),
.cm-form :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: var(--c-primary);
  box-shadow: 0 0 0 2px rgba(26, 35, 50, 0.08);
}

.cm-datepicker {
  width: 100%;
}

.cm-radio-group {
  display: flex;
  gap: 12px;
}

.cm-radio-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border: 1px solid var(--c-border);
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  color: var(--c-primary);
  transition: all 0.2s ease;
  user-select: none;
}

.cm-radio-option:hover {
  border-color: var(--c-primary);
}

.cm-radio-option.active {
  background: var(--c-primary);
  border-color: var(--c-primary);
  color: #fff;
}

.cm-radio-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid currentColor;
  transition: all 0.2s ease;
}

.cm-radio-option.active .cm-radio-dot {
  background: currentColor;
  box-shadow: inset 0 0 0 2px #fff;
}

.cm-dialog-cancel {
  height: 38px;
  padding: 0 22px;
  border: 1px solid var(--c-border);
  border-radius: 10px;
  background: var(--c-surface);
  color: var(--c-primary);
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cm-dialog-cancel:hover {
  border-color: var(--c-primary);
}

.cm-dialog-save {
  height: 38px;
  padding: 0 22px;
  border: none;
  border-radius: 10px;
  background: var(--c-primary);
  color: #fff;
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.cm-dialog-save:hover {
  background: var(--c-primary-light);
}

@media (max-width: 768px) {
  .cm-header {
    flex-direction: column;
  }

  .cm-table-wrap {
    padding: 16px;
    border-radius: var(--radius-sm);
    overflow-x: auto;
  }
  .cm-table-wrap .el-table {
    min-width: 780px;
  }
}
</style>
