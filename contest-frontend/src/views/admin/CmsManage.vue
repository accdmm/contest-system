<template>
  <div>
    <NavBar />
    <div class="container">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="轮播图" name="banner">
          <el-button type="primary" @click="showAdd('banner')" style="margin-bottom:15px">添加轮播图</el-button>
          <el-table :data="banners" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="描述" />
            <el-table-column prop="imageUrl" label="图片" width="200">
              <template #default="{ row }"><img :src="row.imageUrl" style="width:120px;height:60px;object-fit:cover" /></template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column prop="linkUrl" label="跳转链接" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" @click="editItem(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="公告" name="announcement">
          <el-button type="primary" @click="showAdd('announcement')" style="margin-bottom:15px">添加公告</el-button>
          <el-table :data="announcements" border stripe>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="position" label="位置" width="150" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">{{ row.status === 1 ? '展示' : '隐藏' }}</template>
            </el-table-column>
            <el-table-column prop="publishTime" label="发布时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" @click="editItem(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑' : '添加'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item v-if="activeTab === 'announcement'" label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.linkUrl" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item v-if="activeTab === 'announcement'" label="展示位置">
          <el-select v-model="form.position">
            <el-option label="首页滚动" value="home_scroll" />
            <el-option label="消息中心" value="message_center" />
            <el-option label="弹窗" value="popup" />
          </el-select>
        </el-form-item>
        <el-form-item label="定时发布"><el-date-picker v-model="form.publishTime" type="datetime" clearable /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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
.container { max-width: 1000px; margin: 20px auto; }
</style>
