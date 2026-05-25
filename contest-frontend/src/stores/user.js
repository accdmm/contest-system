import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!user.value)
  const isAdmin = computed(() => user.value?.role === 1)
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
  }

  return { user, token, isLoggedIn, isAdmin, userId, setUser, logout }
})
