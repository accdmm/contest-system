<template>
  <div>
    <NavBar />
    <div class="container">
      <el-select v-model="contestId" filterable placeholder="选择竞赛" style="width:300px;margin-bottom:15px" @change="fetchData">
        <el-option v-for="c in contests" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="regType" label="类型" width="80">
          <template #default="{ row }">{{ row.regType === 0 ? '个人' : '团队' }}</template>
        </el-table-column>
        <el-table-column prop="teamId" label="团队ID" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="sType(row.status)">{{ sLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="approve(row.id)">通过</el-button>
              <el-button size="small" type="danger" @click="showReject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" background layout="prev, pager, next" :total="total" :page-size="size" @current-change="pageChange" style="margin-top:15px" />
    </div>

    <el-dialog v-model="rejectVisible" title="驳回原因" width="400px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="驳回原因不少于5个字符" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageContests } from '../../api/contest'
import { pageRegistrationByContest, approveRegistration, rejectRegistration } from '../../api/registration'

const contests = ref([])
const contestId = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(true)
const rejectVisible = ref(false)
const rejectReason = ref('')
const currentRegId = ref(null)

const sMap = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已取消' }
const sTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
const sLabel = s => sMap[s] || ''
const sType = s => sTypeMap[s] || 'info'

async function loadContests() {
  try {
    const res = await pageContests({ page: 1, size: 100 })
    contests.value = res.data.records || []
  } catch (e) { /* ignore */ }
}

async function fetchData() {
  if (!contestId.value) { list.value = []; total.value = 0; return }
  loading.value = true
  try {
    const res = await pageRegistrationByContest(contestId.value, { page: page.value, size })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

async function approve(id) {
  await approveRegistration(id)
  ElMessage.success('已通过')
  fetchData()
}

function showReject(row) {
  currentRegId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (rejectReason.value.length < 5) { ElMessage.warning('驳回原因不少于5个字符'); return }
  await rejectRegistration(currentRegId.value, rejectReason.value)
  ElMessage.success('已驳回')
  rejectVisible.value = false
  fetchData()
}

function pageChange(p) { page.value = p; fetchData() }

onMounted(loadContests)
</script>

<style scoped>
.container { max-width: 1000px; margin: 20px auto; }
</style>
