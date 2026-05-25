import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: () => import('../views/login/Login.vue') },
  { path: '/home', name: 'Home', component: () => import('../views/home/Home.vue') },

  // Contest
  { path: '/contest', name: 'ContestList', component: () => import('../views/contest/ContestList.vue') },
  { path: '/contest/:id', name: 'ContestDetail', component: () => import('../views/contest/ContestDetail.vue') },

  // User
  { path: '/profile', name: 'Profile', component: () => import('../views/user/Profile.vue'), meta: { requiresAuth: true } },

  // Team
  { path: '/my-teams', name: 'MyTeams', component: () => import('../views/team/MyTeam.vue'), meta: { requiresAuth: true } },
  { path: '/team/create', name: 'CreateTeam', component: () => import('../views/team/CreateTeam.vue'), meta: { requiresAuth: true } },
  { path: '/team/:id', name: 'TeamDetail', component: () => import('../views/team/TeamDetail.vue'), meta: { requiresAuth: true } },

  // Registration
  { path: '/my-registration', name: 'MyRegistration', component: () => import('../views/registration/MyRegistration.vue'), meta: { requiresAuth: true } },

  // Notification
  { path: '/notification', name: 'Notification', component: () => import('../views/notification/NotificationList.vue'), meta: { requiresAuth: true } },

  // Admin
  { path: '/admin', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { requiresAuth: true, role: 1 } },
  { path: '/admin/contest', name: 'AdminContest', component: () => import('../views/admin/ContestManage.vue'), meta: { requiresAuth: true, role: 1 } },
  { path: '/admin/review', name: 'AdminReview', component: () => import('../views/admin/ReviewRegistration.vue'), meta: { requiresAuth: true, role: 1 } },
  { path: '/admin/cms', name: 'AdminCms', component: () => import('../views/admin/CmsManage.vue'), meta: { requiresAuth: true, role: 1 } },
  { path: '/admin/notification', name: 'AdminNotification', component: () => import('../views/admin/NotificationManage.vue'), meta: { requiresAuth: true, role: 1 } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { requiresAuth: true, role: 1 } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.requiresAuth && !user) {
    next('/login')
  } else if (to.meta.role !== undefined && user?.role !== to.meta.role) {
    next('/home')
  } else {
    next()
  }
})

export default router
