<template>
  <el-container class="cleaner-layout">
    <el-header class="cleaner-header">
      <div class="header-left">
        <svg viewBox="0 0 32 32" fill="none" width="28" height="28">
          <rect width="32" height="32" rx="8" fill="rgba(255,255,255,0.2)"/>
          <path d="M10 10h12M10 16h12M10 22h8" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span class="header-title">公厕管理系统</span>
        <el-tag size="small" effect="dark" :type="roleTagType" style="margin-left: 12px">
          {{ roleLabel }}
        </el-tag>
      </div>
      <div class="header-nav" v-if="showNav">
        <template v-if="isCleaner">
          <router-link to="/cleaner/tasks" class="nav-link" :class="{ active: $route.path === '/cleaner/tasks' }">
            <el-icon><List /></el-icon> 任务看板
          </router-link>
          <router-link to="/cleaner/schedule" class="nav-link" :class="{ active: $route.path === '/cleaner/schedule' }">
            <el-icon><Calendar /></el-icon> 我的排班
          </router-link>
          <router-link to="/cleaner/incident" class="nav-link" :class="{ active: $route.path === '/cleaner/incident' }">
            <el-icon><Warning /></el-icon> 情况汇报
          </router-link>
        </template>
        <template v-if="isRepairer">
          <router-link to="/repairer/orders" class="nav-link" :class="{ active: $route.path === '/repairer/orders' }">
            <el-icon><Tools /></el-icon> 我的工单
          </router-link>
          <router-link to="/repairer/incident" class="nav-link" :class="{ active: $route.path === '/repairer/incident' }">
            <el-icon><Warning /></el-icon> 情况汇报
          </router-link>
        </template>
      </div>
      <div class="header-right">
        <el-avatar :size="28" style="background: rgba(255,255,255,0.2); color: #fff; font-size: 13px">
          {{ userInfo?.realName?.charAt(0) || 'U' }}
        </el-avatar>
        <span style="color: rgba(255,255,255,0.9); font-size: 14px">{{ userInfo?.realName }}</span>
        <el-button text style="color: rgba(255,255,255,0.8); margin-left: 8px" @click="handleLogout">退出</el-button>
      </div>
      <MessageDrawer v-model="messageDrawerVisible" :unreadCount="unreadMsgCount"
        @update:unreadCount="unreadMsgCount = $event" />
    </el-header>
    <el-main class="cleaner-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUnreadMessageCount } from '../api'
import MessageDrawer from '../components/MessageDrawer.vue'

const router = useRouter()
const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
const unreadMsgCount = ref(0)
const messageDrawerVisible = ref(false)

async function fetchUnreadCount() {
  try { const res = await getUnreadMessageCount(); unreadMsgCount.value = res.data || 0 }
  catch { unreadMsgCount.value = 0 }
}
onMounted(fetchUnreadCount)

const isCleaner = computed(() => !userInfo || userInfo.role === 'CLEANER' || userInfo.role === 'ADMIN')
const isRepairer = computed(() => userInfo && (userInfo.role === 'REPAIR' || userInfo.role === 'ADMIN'))
const showNav = computed(() => userInfo && userInfo.role !== 'ADMIN')
const roleLabel = computed(() => {
  if (!userInfo) return ''
  if (userInfo.role === 'CLEANER') return '保洁人员'
  if (userInfo.role === 'REPAIR') return '维修人员'
  return '管理员'
})
const roleTagType = computed(() => {
  if (!userInfo) return 'info'
  if (userInfo.role === 'CLEANER') return 'success'
  if (userInfo.role === 'REPAIR') return 'warning'
  return ''
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.cleaner-layout { height: 100vh; }

/* ====== 顶部导航 ====== */
.cleaner-header {
  background: linear-gradient(135deg, #0F2027, #1A3650, #1E3A5F);
  background-size: 200% 200%;
  animation: headerShift 8s ease infinite;
  display: flex; align-items: center;
  padding: 0 24px; height: 56px; gap: 24px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.2);
  position: relative; z-index: 10;
}
.cleaner-header::after {
  content: '';
  position: absolute; bottom: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(24,144,255,0.5), rgba(82,196,26,0.3), transparent);
}

@keyframes headerShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.header-left { display: flex; align-items: center; }
.header-title {
  color: #fff; font-size: 17px; font-weight: 700;
  margin-left: 12px; letter-spacing: 0.5px;
}
.header-nav { display: flex; gap: 4px; flex: 1; }
.nav-link {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px;
  color: rgba(255,255,255,0.75); text-decoration: none;
  font-size: 14px; transition: all 0.25s;
}
.nav-link:hover {
  background: rgba(255,255,255,0.12); color: #fff;
  transform: translateY(-1px);
}
.nav-link.active {
  background: rgba(24,144,255,0.3); color: #fff;
  font-weight: 600; box-shadow: 0 2px 8px rgba(24,144,255,0.2);
}
.header-right { display: flex; align-items: center; gap: 10px; margin-left: auto; }

/* ====== 主内容区 ====== */
.cleaner-main {
  background:
    radial-gradient(ellipse at 0% 0%, rgba(24,144,255,0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 100% 100%, rgba(82,196,26,0.02) 0%, transparent 50%),
    linear-gradient(180deg, #f0f2f5, #f8f9fb);
  padding: 24px;
  overflow-y: auto;
}
</style>
