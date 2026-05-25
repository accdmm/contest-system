<template>
  <div>
    <NavBar />
    <div class="container">
      <h2>管理后台</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card @click="$router.push('/admin/contest')" shadow="hover" style="cursor:pointer">
            <el-icon size="40" color="#409eff"><Trophy /></el-icon>
            <h3>竞赛管理</h3>
            <p>发布、编辑、上下架竞赛</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card @click="$router.push('/admin/review')" shadow="hover" style="cursor:pointer">
            <el-icon size="40" color="#67c23a"><DocumentChecked /></el-icon>
            <h3>报名审核</h3>
            <p>审核学生报名申请</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card @click="$router.push('/admin/cms')" shadow="hover" style="cursor:pointer">
            <el-icon size="40" color="#e6a23c"><Setting /></el-icon>
            <h3>内容管理</h3>
            <p>轮播图、公告管理</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import NavBar from '../../components/NavBar.vue'
</script>

<style scoped>
.container { max-width: 900px; margin: 20px auto; }
.el-card { text-align: center; padding: 30px 0; }
.el-card h3 { margin: 10px 0 5px; }
</style>
