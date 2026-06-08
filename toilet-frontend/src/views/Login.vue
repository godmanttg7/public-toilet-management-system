<template>
  <div class="login-page">
    <!-- 动态光晕背景 -->
    <div class="login-bg-glow">
      <div class="glow-orb glow-orb-1"></div>
      <div class="glow-orb glow-orb-2"></div>
      <div class="glow-orb glow-orb-3"></div>
    </div>

    <!-- 网格线背景 -->
    <div class="login-grid"></div>

    <div class="login-container">
      <div class="login-card">
        <!-- 顶部光条 -->
        <div class="login-card-topbar"></div>

        <div class="login-header">
          <div class="login-icon pulse-glow-icon">
            <svg viewBox="0 0 40 40" fill="none" width="48" height="48">
              <rect width="40" height="40" rx="12" fill="url(#iconGrad)"/>
              <path d="M12 14h16M12 20h16M12 26h12" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/>
              <defs>
                <linearGradient id="iconGrad" x1="0" y1="0" x2="40" y2="40">
                  <stop offset="0%" stop-color="#1890FF"/>
                  <stop offset="100%" stop-color="#096DD9"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1>公厕智慧管理系统</h1>
          <p>Public Toilet Management System</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
              class="login-input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
              class="login-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              @click="handleLogin"
              :loading="loading"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>测试账号：admin / 123456</span>
          <el-divider direction="vertical" />
          <el-link type="primary" :underline="false" @click="$router.push('/')">返回首页</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const data = res.data
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))
    if (data.role === 'ADMIN') router.push('/admin/dashboard')
    else if (data.role === 'CLEANER') router.push('/cleaner/tasks')
    else if (data.role === 'REPAIR') router.push('/repairer/orders')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ====== 页面容器 ====== */
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0F2027, #1A3650, #1E3A5F);
  position: relative;
  overflow: hidden;
}

/* ====== 动态光晕球 ====== */
.login-bg-glow { position: absolute; inset: 0; pointer-events: none; }
.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.25;
  animation: orbDrift 12s ease-in-out infinite;
}
.glow-orb-1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(24,144,255,0.5), transparent);
  top: -15%; right: -10%;
  animation-delay: 0s;
}
.glow-orb-2 {
  width: 350px; height: 350px;
  background: radial-gradient(circle, rgba(82,196,26,0.4), transparent);
  bottom: -10%; left: -8%;
  animation-delay: -4s;
}
.glow-orb-3 {
  width: 250px; height: 250px;
  background: radial-gradient(circle, rgba(114,46,209,0.35), transparent);
  top: 50%; left: 55%;
  animation-delay: -8s;
}

@keyframes orbDrift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(40px, -30px) scale(1.1); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(-30px, -15px) scale(1.05); }
}

/* ====== 动态网格背景 ====== */
.login-grid {
  position: absolute; inset: 0; pointer-events: none; opacity: 0.06;
  background-image:
    linear-gradient(rgba(255,255,255,0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.3) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridScroll 20s linear infinite;
}

@keyframes gridScroll {
  0% { background-position: 0 0; }
  100% { background-position: 60px 60px; }
}

/* ====== 卡片容器 ====== */
.login-container { position: relative; z-index: 1; }
.login-card {
  width: 420px;
  padding: 0 40px 36px;
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0,0,0,0.35), 0 0 0 1px rgba(255,255,255,0.15);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
  animation: cardSlideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes cardSlideIn {
  from { opacity: 0; transform: translateY(40px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 顶部渐变光条 */
.login-card-topbar {
  height: 4px;
  background: linear-gradient(90deg, #1890FF, #52C41A, #FAAD14, #1890FF);
  background-size: 300% 100%;
  animation: gradientShift 4s linear infinite;
  margin: 0 -40px 40px;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  100% { background-position: 300% 50%; }
}

/* ====== Header ====== */
.login-header { text-align: center; margin-bottom: 36px; }
.login-icon {
  display: inline-block; margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.login-header h1 {
  font-size: 24px; font-weight: 700; color: #1E3A5F;
  margin: 0 0 6px; letter-spacing: 1px;
}
.login-header p {
  font-size: 12px; color: #9CA3AF; margin: 0;
  letter-spacing: 2px; text-transform: uppercase;
}

/* ====== 输入框 ====== */
.login-input :deep(.el-input__wrapper) {
  background: #f7f8fa !important;
  border: 1px solid #e8ecf1 !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  padding: 8px 14px;
  transition: all 0.3s;
}
.login-input :deep(.el-input__wrapper:hover) {
  border-color: #b0d4ff !important;
  background: #fff !important;
}
.login-input :deep(.el-input__wrapper.is-focus) {
  border-color: #1890FF !important;
  box-shadow: 0 0 0 3px rgba(24,144,255,0.1) !important;
  background: #fff !important;
}

/* ====== 登录按钮 ====== */
.login-btn {
  width: 100%; height: 46px; font-size: 16px; font-weight: 600;
  letter-spacing: 4px; border-radius: 10px;
  background: linear-gradient(135deg, #1890FF, #096DD9) !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(24,144,255,0.35);
  transition: all 0.3s;
}
.login-btn:hover {
  box-shadow: 0 8px 28px rgba(24,144,255,0.5);
  transform: translateY(-1px);
}
.login-btn:active { transform: translateY(0); }

/* ====== Footer ====== */
.login-footer {
  text-align: center; margin-top: 20px;
  font-size: 13px; color: #9CA3AF;
}

/* ====== 脉冲光晕图标 ====== */
.pulse-glow-icon {
  animation: iconPulse 3s ease-in-out infinite;
}
@keyframes iconPulse {
  0%, 100% { filter: drop-shadow(0 0 8px rgba(24,144,255,0.2)); }
  50% { filter: drop-shadow(0 0 24px rgba(24,144,255,0.5)); }
}
</style>
