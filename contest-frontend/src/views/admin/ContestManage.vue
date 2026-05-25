<template>
  <div>
    <NavBar />
    <div class="container">
      <div style="margin-bottom:15px">
        <el-button type="primary" @click="dialogVisible = true; isEdit = false; form = {}">新建竞赛</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="level" label="级别" width="80" />
        <el-table-column prop="currentCount" label="报名数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="publish(row.id)">上架</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="unpublish(row.id)">下架</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="pageChange" style="margin-top:15px" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑竞赛' : '新建竞赛'" width="700px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类别" required>
          <el-select v-model="form.category">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别" required>
          <el-select v-model="form.level">
            <el-option label="校级" value="校级" /><el-option label="省级" value="省级" /><el-option label="国家级" value="国家级" />
          </el-select>
        </el-form-item>
        <el-form-item label="主办方"><el-input v-model="form.organizer" /></el-form-item>
        <el-form-item label="竞赛时间"><el-date-picker v-model="form.contestTime" type="datetime" format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="报名开始"><el-date-picker v-model="form.registerStartTime" type="datetime" /></el-form-item>
        <el-form-item label="报名截止"><el-date-picker v-model="form.registerEndTime" type="datetime" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="参赛形式" required>
          <el-radio-group v-model="form.contestType">
            <el-radio :value="0">仅个人赛</el-radio>
            <el-radio :value="1">仅团队赛</el-radio>
            <el-radio :value="2">两者皆可</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="人数上限"><el-input-number v-model="form.maxParticipants" :min="0" /></el-form-item>
        <el-form-item label="封面图"><el-input v-model="form.coverImageUrl" placeholder="MinIO URL" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageContests, createContest, updateContest, publishContest, unpublishContest, deleteContest } from '../../api/contest'

const categories = ['理工类', '文史类', '艺术类', '体育类', '创新创业类']
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(true)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({})

const statusMap = { 0: '未发布', 1: '报名中', 2: '已截止' }
const statusTypeMap = { 0: 'info', 1: 'success', 2: 'warning' }
const statusLabel = s => statusMap[s] || ''
const statusType = s => statusTypeMap[s] || 'info'

async function fetchData() {
  loading.value = true
  try {
    const res = await pageContests({ page: page.value, size })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

function edit(row) {
  Object.assign(form, row)
  isEdit.value = true
  dialogVisible.value = true
}

async function save() {
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
  } finally { saving.value = false }
}

async function publish(id) { await publishContest(id); ElMessage.success('已上架'); fetchData() }
async function unpublish(id) { await unpublishContest(id); ElMessage.success('已下架'); fetchData() }

async function del(id) {
  await ElMessageBox.confirm('确定删除该竞赛吗？')
  await deleteContest(id)
  ElMessage.success('已删除')
  fetchData()
}

function pageChange(p) { page.value = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.container { max-width: 1200px; margin: 20px auto; }
</style>
