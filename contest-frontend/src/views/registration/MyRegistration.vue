<template>
  <div>
    <NavBar />
    <div class="container">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="报名ID" width="80" />
        <el-table-column prop="contestId" label="竞赛ID" width="80" />
        <el-table-column prop="regType" label="类型" width="100">
          <template #default="{ row }">{{ row.regType === 0 ? '个人赛' : '团队赛' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewReason" label="驳回原因" />
        <el-table-column prop="createTime" label="报名时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        style="margin-top:20px"
        @current-change="pageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageRegistrationByUser, cancelRegistration } from '../../api/registration'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(true)

const statusMap = { 0: { label: '待审核', type: 'warning' }, 1: { label: '已通过', type: 'success' }, 2: { label: '已驳回', type: 'danger' }, 3: { label: '已取消', type: 'info' } }
const statusLabel = s => statusMap[s]?.label || '未知'
const statusType = s => statusMap[s]?.type || 'info'

async function fetchData() {
  loading.value = true
  try {
    const res = await pageRegistrationByUser(store.userId, { page: page.value, size })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignore */ } finally { loading.value = false }
}

async function cancel(row) {
  await cancelRegistration(row.id, store.userId)
  ElMessage.success('已取消')
  fetchData()
}

function pageChange(p) { page.value = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.container { max-width: 1000px; margin: 20px auto; }
</style>
