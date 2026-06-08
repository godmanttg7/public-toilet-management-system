<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo" v-if="!isCollapse">
          <svg viewBox="0 0 32 32" fill="none" width="32" height="32">
            <rect width="32" height="32" rx="8" fill="#1890FF" opacity="0.15"/>
            <path d="M10 10h12M10 16h12M10 22h8" stroke="#1890FF" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>公厕管理系统</span>
        </div>
        <div class="sidebar-logo sidebar-logo-collapsed" v-else>
          <svg viewBox="0 0 32 32" fill="none" width="28" height="28">
            <rect width="32" height="32" rx="8" fill="#1890FF" opacity="0.15"/>
            <path d="M10 10h12M10 16h12M10 22h8" stroke="#1890FF" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="transparent"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
        router
        :collapse="isCollapse"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-menu-item index="/admin/toilet">
          <el-icon><Location /></el-icon>
          <template #title>公厕管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/schedule">
          <el-icon><Calendar /></el-icon>
          <template #title>保洁排班</template>
        </el-menu-item>
        <el-menu-item index="/admin/facility">
          <el-icon><Setting /></el-icon>
          <template #title>设施管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/repair">
          <el-icon><Tools /></el-icon>
          <template #title>维修工单</template>
        </el-menu-item>
        <el-menu-item index="/admin/consumable">
          <el-icon><Box /></el-icon>
          <template #title>耗材管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/feedback">
          <el-icon><ChatLineSquare /></el-icon>
          <template #title>公众反馈</template>
        </el-menu-item>
        <el-menu-item index="/admin/incident">
          <el-icon><WarningFilled /></el-icon>
          <template #title>情况汇报</template>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-button text @click="isCollapse = !isCollapse" class="collapse-btn">
            <el-icon :size="20"><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="pageTitle">{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-link href="/" :underline="false" style="margin-right: 8px">
            <el-icon><HomeFilled /></el-icon> 返回前台
          </el-link>
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" style="background: #1890FF">{{ userInfo?.realName?.charAt(0) || 'U' }}</el-avatar>
              <span class="user-name">{{ userInfo?.realName || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)

const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '')

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    .then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    })
}
</script>

<style scoped>
.admin-layout { height: 100vh; }

/* ====== 侧边栏 ====== */
.admin-sidebar {
  background: linear-gradient(180deg, #001529 0%, #001a35 50%, #001F3F 100%) !important;
  overflow: hidden;
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}
.admin-sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 20%, rgba(24,144,255,0.08) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 80%, rgba(82,196,26,0.05) 0%, transparent 60%);
  pointer-events: none;
}
.admin-sidebar::after {
  content: '';
  position: absolute;
  right: 0; top: 0; bottom: 0;
  width: 1px;
  background: linear-gradient(180deg,
    transparent 0%,
    rgba(24,144,255,0.3) 20%,
    rgba(24,144,255,0.15) 50%,
    rgba(24,144,255,0.3) 80%,
    transparent 100%
  );
}

.sidebar-header {
  height: 64px;
  display: flex; align-items: center; justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  position: relative;
}
.sidebar-header::after {
  content: '';
  position: absolute;
  bottom: -1px; left: 20%; right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(24,144,255,0.4), transparent);
}
.sidebar-logo {
  display: flex; align-items: center; gap: 10px;
  color: #fff; font-size: 16px; font-weight: 700; letter-spacing: 0.5px;
}

/* ====== 顶部栏 ====== */
.admin-header {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; height: 64px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  z-index: 10; position: relative;
}

.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn {
  padding: 6px; color: #666;
  border-radius: 8px;
  transition: all 0.2s;
}
.collapse-btn:hover { background: #f0f5ff; color: #1890FF; }

.header-right { display: flex; align-items: center; gap: 20px; }
.header-badge { cursor: pointer; }

.user-info {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; padding: 4px 10px;
  border-radius: 8px;
  transition: all 0.25s;
}
.user-info:hover { background: #f0f5ff; }
.user-name { font-size: 14px; color: #333; font-weight: 500; }

/* ====== 主内容区 ====== */
.admin-main {
  background:
    radial-gradient(ellipse at 0% 0%, rgba(24,144,255,0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 100% 100%, rgba(82,196,26,0.02) 0%, transparent 50%),
    #f0f2f5;
  padding: 24px;
  overflow-y: auto;
  position: relative;
}
</style>
