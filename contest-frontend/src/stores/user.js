import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  let savedUser = null
  try { savedUser = JSON.parse(localStorage.getItem('user') || 'null') } catch { savedUser = null }
  const user = ref(savedUser)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 1)
  const isTeacher = computed(() => user.value?.role === 2)
  const userId = computed(() => user.value?.id)

  function setUser(userData, tokenStr) {
    user.value = userData
    token.value = tokenStr
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('token', tokenStr)
  }

  function logout() {
    user.value = null
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    localStorage.removeItem('ai_messages')
  }

  return { user, token, isLoggedIn, isAdmin, isTeacher, userId, setUser, logout }
})
