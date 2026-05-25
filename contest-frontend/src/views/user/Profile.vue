<template>
  <div>
    <NavBar />
    <div class="container">
      <el-card>
        <template #header>个人信息</template>
        <el-form :model="form" label-width="100px">
          <el-form-item label="学号">
            <el-input v-model="form.username" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="form.college" disabled />
          </el-form-item>
          <el-form-item label="专业">
            <el-input v-model="form.major" disabled />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleUpdate">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card style="margin-top:20px">
        <template #header>修改密码</template>
        <el-form :model="pwdForm" label-width="100px">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePwd">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../../components/NavBar.vue'
import { getUserById, updateProfile, changePassword } from '../../api/user'
import { useUserStore } from '../../stores/user'

const store = useUserStore()
const form = reactive({ username: '', name: '', email: '', phone: '', college: '', major: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

onMounted(async () => {
  try {
    const res = await getUserById(store.userId)
    Object.assign(form, res.data)
  } catch (e) { /* ignore */ }
})

async function handleUpdate() {
  await updateProfile(store.userId, form)
  ElMessage.success('保存成功')
}

async function handleChangePwd() {
  await changePassword(store.userId, pwdForm)
  ElMessage.success('密码修改成功')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
}
</script>

<style scoped>
.container { max-width: 600px; margin: 20px auto; }
</style>
