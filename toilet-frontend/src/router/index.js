import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'toilet',
        name: 'ToiletManage',
        component: () => import('../views/admin/ToiletManage.vue'),
        meta: { title: '公厕管理' }
      },
      {
        path: 'schedule',
        name: 'ScheduleManage',
        component: () => import('../views/admin/ScheduleManage.vue'),
        meta: { title: '保洁排班' }
      },
      {
        path: 'facility',
        name: 'FacilityManage',
        component: () => import('../views/admin/FacilityManage.vue'),
        meta: { title: '设施管理' }
      },
      {
        path: 'repair',
        name: 'RepairOrderManage',
        component: () => import('../views/admin/RepairOrderManage.vue'),
        meta: { title: '维修工单' }
      },
      {
        path: 'consumable',
        name: 'ConsumableManage',
        component: () => import('../views/admin/ConsumableManage.vue'),
        meta: { title: '耗材管理' }
      },
      {
        path: 'feedback',
        name: 'FeedbackManage',
        component: () => import('../views/admin/FeedbackManage.vue'),
        meta: { title: '公众反馈' }
      },
      {
        path: 'incident',
        name: 'IncidentManage',
        component: () => import('../views/admin/IncidentManage.vue'),
        meta: { title: '情况汇报' }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      }
    ]
  },
  {
    path: '/cleaner',
    component: () => import('../layout/CleanerLayout.vue'),
    meta: { requiresAuth: true, role: ['CLEANER', 'ADMIN'] },
    redirect: '/cleaner/tasks',
    children: [
      {
        path: 'tasks',
        name: 'TaskBoard',
        component: () => import('../views/cleaner/TaskBoard.vue'),
        meta: { title: '任务看板' }
      },
      {
        path: 'schedule',
        name: 'MySchedule',
        component: () => import('../views/cleaner/MySchedule.vue'),
        meta: { title: '我的排班' }
      },
      {
        path: 'incident',
        name: 'CleanerIncident',
        component: () => import('../views/cleaner/IncidentReport.vue'),
        meta: { title: '情况汇报' }
      }
    ]
  },
  {
    path: '/repairer',
    component: () => import('../layout/CleanerLayout.vue'),
    meta: { requiresAuth: true, role: ['REPAIR', 'ADMIN'] },
    redirect: '/repairer/orders',
    children: [
      {
        path: 'orders',
        name: 'RepairerOrders',
        component: () => import('../views/cleaner/RepairerOrders.vue'),
        meta: { title: '我的工单' }
      },
      {
        path: 'incident',
        name: 'RepairerIncident',
        component: () => import('../views/cleaner/IncidentReport.vue'),
        meta: { title: '情况汇报' }
      }
    ]
  },
  {
    path: '/public',
    name: 'PublicMap',
    component: () => import('../views/public/ToiletMap.vue'),
    meta: { title: '公厕查询' }
  },
  {
    path: '/public/feedback/:toiletId',
    name: 'FeedbackForm',
    component: () => import('../views/public/FeedbackForm.vue'),
    meta: { title: '满意度评价' }
  },
  {
    path: '/',
    name: 'Showcase',
    component: () => import('../views/public/Showcase.vue'),
    meta: { title: '智慧公厕' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 公厕管理系统` : '公厕管理系统'

  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')

    if (!token) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }

    if (to.meta.role && userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      const requiredRole = to.meta.role
      if (Array.isArray(requiredRole)) {
        if (!requiredRole.includes(userInfo.role)) {
          ElMessage.error('无权访问此页面')
          next(from.path || '/login')
          return
        }
      } else {
        if (userInfo.role !== requiredRole) {
          ElMessage.error('无权访问此页面')
          next(from.path || '/login')
          return
        }
      }
    }
  }

  next()
})

export default router
