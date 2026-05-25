<template>
  <div>
    <NavBar />
    <div class="container">
      <div style="margin-bottom:15px; display:flex; justify-content:space-between; align-items:center">
        <span>
          未读 <el-tag>{{ unreadCount }}</el-tag>
        </span>
        <el-button size="small" @click="markAll">全部标为已读</el-button>
      </div>

      <el-timeline>
        <el-timeline-item
          v-for="n in list"
          :key="n.id"
          :timestamp="n.createTime"
          :color="n.isRead === 0 ? '#409eff' : '#c0c4cc'"
        >
          <el-card shadow="hover" :class="{ unread: n.isRead === 0 }" @click="handleClick(n)">
            <h4>{{ n.title }}</h4>
            <p>{{ n.content }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        @current-change="pageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { pageNotificationByUser, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '../../api/notification'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const list = ref([])
const total = ref(0)
const unreadCount = ref(0)
const page = ref(1)
const size = 10

async function fetchData() {
  try {
    const [notifRes, unreadRes] = await Promise.all([
      pageNotificationByUser(store.userId, { page: page.value, size }),
      getUnreadCount(store.userId)
    ])
    list.value = notifRes.data.records || []
    total.value = notifRes.data.total || 0
    unreadCount.value = unreadRes.data || 0
  } catch (e) { /* ignore */ }
}

async function handleClick(n) {
  if (n.isRead === 0) {
    await markNotificationRead(n.id, store.userId)
    n.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

async function markAll() {
  await markAllNotificationsRead(store.userId)
  ElMessage.success('已全部标为已读')
  fetchData()
}

function pageChange(p) { page.value = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.container { max-width: 800px; margin: 20px auto; }
.unread { border-left: 3px solid #409eff; }
.el-card { cursor: pointer; }
</style>
