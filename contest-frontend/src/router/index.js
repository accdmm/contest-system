import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

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

  // Teacher
  { path: '/teacher/teams', name: 'TeacherTeams', component: () => import('../views/teacher/TeacherTeams.vue'), meta: { requiresAuth: true, role: 2 } },

  // Notification
  { path: '/notification', name: 'Notification', component: () => import('../views/notification/NotificationList.vue'), meta: { requiresAuth: true } },

  // Admin (with layout)
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 1 },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'contest', name: 'AdminContest', component: () => import('../views/admin/ContestManage.vue') },
      { path: 'review', name: 'AdminReview', component: () => import('../views/admin/ReviewRegistration.vue') },
      { path: 'cms', name: 'AdminCms', component: () => import('../views/admin/CmsManage.vue') },
      { path: 'notification', name: 'AdminNotification', component: () => import('../views/admin/NotificationManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue') },
      { path: 'permissions', name: 'AdminPermissions', component: () => import('../views/admin/PermissionManage.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const store = useUserStore()
  if (to.meta.requiresAuth && !store.isLoggedIn) {
    next('/login')
  } else if (to.meta.role !== undefined && store.user?.role !== to.meta.role) {
    next('/home')
  } else {
    next()
  }
})

router.afterEach(() => window.scrollTo(0, 0))

export default router
