<template>
  <div>
    <NavBar />
    <div class="container">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="竞赛名称" clearable />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="query.category" clearable placeholder="全部">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部">
            <el-option label="报名中" :value="1" />
            <el-option label="已截止" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20">
        <el-col :span="6" v-for="c in list" :key="c.id" style="margin-bottom:20px">
          <ContestCard :contest="c" />
        </el-col>
      </el-row>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="query.size"
        @current-change="pageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import NavBar from '../../components/NavBar.vue'
import ContestCard from '../../components/ContestCard.vue'
import { pageContests } from '../../api/contest'

const categories = ['理工类', '文史类', '艺术类', '体育类', '创新创业类']
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', category: '', status: '', page: 1, size: 12 })

async function fetchData() {
  const params = { ...query }
  if (!params.status) delete params.status
  if (!params.category) delete params.category
  if (!params.keyword) delete params.keyword
  try {
    const res = await pageContests(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) { /* ignore */ }
}

function search() { query.page = 1; fetchData() }
function pageChange(p) { query.page = p; fetchData() }

onMounted(fetchData)
</script>

<style scoped>
.container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }
</style>
