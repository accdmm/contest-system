<template>
  <el-menu mode="horizontal" :ellipsis="false" router>
    <el-menu-item index="/home">
      <el-icon><HomeFilled /></el-icon>
      首页
    </el-menu-item>
    <el-menu-item index="/contest">
      <el-icon><TrophyBase /></el-icon>
      竞赛列表
    </el-menu-item>

    <template v-if="store.isLoggedIn">
      <el-menu-item index="/my-registration">
        <el-icon><Document /></el-icon>
        我的报名
      </el-menu-item>
      <el-menu-item index="/notification">
        <el-icon><Bell /></el-icon>
        通知
      </el-menu-item>
      <el-sub-menu index="user">
        <template #title>
          <el-icon><User /></el-icon>
          {{ store.user?.name }}
        </template>
        <el-menu-item index="/profile">个人信息</el-menu-item>
        <el-menu-item index="/team/create">创建团队</el-menu-item>
        <el-menu-item v-if="store.isAdmin" index="/admin">管理后台</el-menu-item>
        <el-menu-item @click="handleLogout">退出登录</el-menu-item>
      </el-sub-menu>
    </template>

    <el-menu-item v-else index="/login">
      <el-icon><Key /></el-icon>
      登录
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const store = useUserStore()
const router = useRouter()

function handleLogout() {
  store.logout()
  router.push('/login')
}
</script>
