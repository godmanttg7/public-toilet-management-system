<template>
  <div class="showcase">
    <!-- 滚动进度条 -->
    <div class="showcase-scrollbar">
      <div class="showcase-scrollbar__fill" :style="{ width: scrollPercent + '%' }"></div>
    </div>

    <!-- ====== Section 1: Hero ====== -->
    <section class="sc-section sc-hero">
      <!-- 动态光晕 -->
      <div class="sc-hero__aurora">
        <div class="sc-hero__orb sc-hero__orb--1"></div>
        <div class="sc-hero__orb sc-hero__orb--2"></div>
        <div class="sc-hero__orb sc-hero__orb--3"></div>
      </div>
      <!-- 网格线 -->
      <div class="sc-hero__grid"></div>

      <div class="sc-hero__content">
        <div class="sc-hero__badge-bar">
          <span class="sc-hero__badge-dot"></span>
          <h2 class="sc-hero__badge">UOS 智慧公厕系统</h2>
        </div>
        <h1 class="sc-hero__title">
          智领未来。<span class="sc-hero__title--gradient">安全可靠。</span>
        </h1>
        <p class="sc-hero__desc">
          基于国产化架构，重新定义城市公共服务体验。
        </p>

        <!-- 数据亮点 -->
        <div class="sc-hero__stats">
          <div class="sc-hero__stat">
            <span class="sc-hero__stat-num">{{ displayToilets }}</span>
            <span class="sc-hero__stat-label">智慧公厕</span>
          </div>
        </div>

        <div class="sc-hero__actions">
          <router-link to="/public" class="sc-btn sc-btn--primary">
            <el-icon><Search /></el-icon> 查询公厕
          </router-link>
          <a href="#feedback" class="sc-btn sc-btn--ghost">
            <el-icon><Edit /></el-icon> 我要反馈
          </a>
          <a href="/login" class="sc-btn sc-btn--ghost">
            <el-icon><User /></el-icon> 管理员入口
          </a>
        </div>
        <div v-if="loading" class="sc-hero__loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在连接后端服务...</span>
        </div>
      </div>

      <!-- 向下滚动提示 -->
      <div class="sc-hero__scroll-hint">
        <div class="sc-hero__scroll-mouse">
          <div class="sc-hero__scroll-wheel"></div>
        </div>
        <span>向下滚动</span>
      </div>
    </section>

    <!-- ====== Section 2: 全景概览 ====== -->
    <section class="sc-section sc-overview" ref="overviewSection">
      <div class="sc-section__header">
        <h2 class="sc-section__title reveal-text">全景概览。</h2>
        <p class="sc-section__sub reveal-text">实时数据，尽在掌握。</p>
      </div>

      <div class="sc-bento">
        <!-- 客流量卡片 -->
        <div class="sc-card sc-card--traffic reveal-card">
          <div class="sc-card__header">
            <h3 class="sc-card__title">今日客流统计</h3>
            <el-icon :size="24" color="#1890FF"><UserFilled /></el-icon>
          </div>
          <div class="sc-card__body">
            <div class="sc-traffic__value">{{ displayVisits.toLocaleString() }}</div>
            <div class="sc-traffic__growth">
              <el-icon color="#52C41A"><Top /></el-icon>
              <span>实时统计今日客流量</span>
            </div>
          </div>
          <div class="sc-traffic__bars">
            <div
              v-for="(bar, i) in trafficBars"
              :key="i"
              class="sc-traffic__bar"
              :style="{ height: bar + '%', animationDelay: i * 0.05 + 's' }"
            ></div>
          </div>
        </div>

        <!-- 系统状态卡片 -->
        <div class="sc-card sc-card--status reveal-card">
          <div class="sc-card__header">
            <h3 class="sc-card__title">系统运行状态</h3>
            <span class="sc-card__badge pulse-dot">运行正常</span>
          </div>
          <div class="sc-card__body sc-status__info">
            <el-icon :size="48" color="#52C41A"><CircleCheckFilled /></el-icon>
            <span style="font-size: 16px; color: rgba(255,255,255,0.75); margin-top: 8px;">所有系统正常运行中</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== Section 3: 运维团队 ====== -->
    <section class="sc-section sc-staff">
      <div class="sc-section__header sc-section__header--center">
        <h2 class="sc-section__title reveal-text">运维团队</h2>
      </div>

      <div class="sc-staff__grid">
        <div v-for="staff in data.staffList" :key="staff.id" class="sc-staff__card">
          <div class="sc-staff__avatar">
            {{ staff.name?.charAt(0) || '?' }}
          </div>
          <h3 class="sc-staff__name">{{ staff.name }}</h3>
          <p class="sc-staff__role">{{ staff.role }}</p>
          <div class="sc-staff__divider"></div>
          <div class="sc-staff__status">
            <span class="sc-staff__status-dot sc-staff__status-dot--active"></span>
            状态：{{ staff.statusLabel || '在岗' }}
          </div>
        </div>
        <div v-if="!data.staffList || data.staffList.length === 0" class="sc-empty">
          <el-empty description="暂无运维人员" :image-size="80" />
        </div>
      </div>

      <!-- 耗材监测 - 平铺展示 -->
      <div class="sc-consumable" v-if="data.consumables && data.consumables.length > 0">
        <h3 class="sc-consumable__title">耗材监测</h3>
        <div v-for="(group, gi) in consumableGroups" :key="gi" class="sc-consumable__group">
          <div class="sc-consumable__group-header">
            <el-icon :size="18" color="#40A9FF"><Location /></el-icon>
            <span>{{ group.toiletName }}</span>
          </div>
          <div class="sc-consumable__items">
            <div v-for="item in group.items" :key="item.id" class="sc-consumable__card">
              <div class="sc-consumable__card-icon">{{ item.consumableName?.charAt(0) || '?' }}</div>
              <div class="sc-consumable__card-info">
                <div class="sc-consumable__card-row">
                  <span class="sc-consumable__card-name">{{ item.consumableName }}</span>
                  <span class="sc-consumable__card-pct">{{ item.remainingPercent }}%</span>
                </div>
                <div class="sc-consumable__bar">
                  <div class="sc-consumable__fill" :class="{ 'sc-consumable__fill--low': item.isLow }" :style="{ width: item.remainingPercent + '%' }"></div>
                </div>
                <div class="sc-consumable__card-footer">
                  <span>库存 {{ item.currentStock }}{{ item.unit }}</span>
                  <el-tag v-if="item.isLow" type="danger" size="small">{{ item.consumableName }}不足</el-tag>
                  <el-tag v-else type="success" size="small">充足</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Section 4: 反馈表单 -->
    <section id="feedback" class="sc-section sc-feedback">
      <div class="sc-feedback__container">
        <div class="sc-feedback__intro">
          <div class="sc-feedback__intro-icon">
            <el-icon :size="24" color="#1890FF"><ChatDotSquare /></el-icon>
          </div>
          <h2>共建美好环境。</h2>
          <p>发现卫生问题或设备故障？<br/>您的反馈将直达运维中心。</p>
        </div>

        <el-form :model="form" class="sc-feedback__form" @submit.prevent="handleSubmit">
          <el-form-item label="选择公厕">
            <el-select v-model="form.toiletId" placeholder="请选择公厕" filterable style="width: 100%">
              <el-option v-for="t in toiletOptions" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <div class="sc-feedback__row">
            <el-form-item label="问题类型">
              <el-select v-model="form.category" style="width: 100%">
                <el-option label="卫生清洁" value="卫生清洁" />
                <el-option label="设备故障" value="设备故障" />
                <el-option label="耗材缺失" value="耗材缺失" />
                <el-option label="其他建议" value="其他建议" />
              </el-select>
            </el-form-item>
            <el-form-item label="联系方式（选填）">
              <el-input v-model="form.contact" placeholder="手机号" />
            </el-form-item>
          </div>
          <el-form-item label="满意度评分">
            <el-rate v-model="form.score" show-text :texts="['很差', '较差', '一般', '较好', '很好']" />
          </el-form-item>
          <el-form-item label="问题描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述您遇到的问题..."
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleSubmit"
            :loading="submitting"
            :disabled="submitting"
            style="width: 100%"
          >
            <template v-if="submitSuccess">
              <el-icon><Check /></el-icon> 提交成功！
            </template>
            <template v-else>
              <el-icon><Promotion /></el-icon> 提交反馈
            </template>
          </el-button>
        </el-form>
      </div>

      <!-- 底部版权 -->
      <div class="sc-footer">
        <div class="sc-footer__brand">
          <el-icon :size="14"><Monitor /></el-icon>
          <span>公厕智慧管理系统 · 公众服务平台</span>
        </div>
        <div class="sc-footer__links">
          <span>隐私政策</span>
          <span>使用条款</span>
          <span>技术支持</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(true)
const submitting = ref(false)
const submitSuccess = ref(false)
const toiletOptions = ref([])
const scrollPercent = ref(0)
const displayVisits = ref(0)
const displayToilets = ref(0)

const data = reactive({
  totalToilets: 0,
  todayVisits: 0,
  staffList: [],
  consumables: []
})

const form = reactive({
  toiletId: '',
  category: '卫生清洁',
  description: '',
  contact: '',
  score: 5
})

// 耗材按公厕分组
const consumableGroups = computed(() => {
  const groups = []
  const map = {}
  for (const item of data.consumables || []) {
    const key = item.toiletName || ('公厕 #' + (item.toiletId || '?'))
    if (!map[key]) {
      map[key] = { toiletName: key, items: [] }
      groups.push(map[key])
    }
    map[key].items.push(item)
  }
  return groups
})

const trafficBars = [40, 60, 45, 80, 55, 90, 70, 85, 60, 75, 50, 95]

// ====== 数字滚动画 ======
function animateNumber(target, duration = 1500) {
  const start = target
  const startTime = Date.now()
  const timer = setInterval(() => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    // easeOutExpo
    const eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    displayVisits.value = Math.floor(start * eased)
    if (progress >= 1) clearInterval(timer)
  }, 16)
}

// ====== 滚动揭示动画 ======
function setupRevealObserver() {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('revealed')
      }
    })
  }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' })

  document.querySelectorAll('.reveal-text, .reveal-card, .reveal-up').forEach(el => {
    observer.observe(el)
  })
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/public/showcase')
    if (res.data) {
      Object.assign(data, res.data)
    }
  } catch (e) {
    console.error('加载展示数据失败', e)
  } finally {
    loading.value = false
    // 数字滚动画
    setTimeout(() => {
      animateNumber(data.todayVisits || 1248)
      // 公厕数量滚动
      let start = 0, target = data.totalToilets || 0
      const timer = setInterval(() => {
        start += Math.ceil(target / 20)
        if (start >= target) { start = target; clearInterval(timer) }
        displayToilets.value = start
      }, 30)
    }, 400)
  }
}

async function handleSubmit() {
  if (!form.toiletId) {
    ElMessage.warning('请先选择要反馈的公厕')
    return
  }
  if (!form.description.trim()) {
    ElMessage.warning('请填写问题描述')
    return
  }
  submitting.value = true
  try {
    await request.post('/public/feedback', {
      toiletId: form.toiletId,
      category: form.category,
      description: form.description,
      contact: form.contact,
      score: form.score
    })
    submitSuccess.value = true
    form.description = ''
    form.contact = ''
    setTimeout(() => { submitSuccess.value = false }, 3000)
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

function handleScroll() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  scrollPercent.value = docHeight > 0 ? Math.round((scrollTop / docHeight) * 100) : 0
}

onMounted(async () => {
  await fetchData()
  await nextTick()
  setupRevealObserver()
  window.addEventListener('scroll', handleScroll)
  // 加载公厕列表供反馈选择
  try {
    const res = await request.get('/public/toilets')
    if (res.data) toiletOptions.value = res.data
  } catch {}
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* ============================================
   Showcase 首页 - 优化版样式
   ============================================ */

.showcase {
  min-height: 100vh;
  background: #06070a;
  color: #fff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow-x: hidden;
}

/* ====== 滚动进度条 ====== */
.showcase-scrollbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: 3px; background: rgba(255,255,255,0.03);
  backdrop-filter: blur(10px);
}
.showcase-scrollbar__fill {
  height: 100%;
  background: linear-gradient(90deg, #1890FF, #52C41A, #1890FF);
  background-size: 200% 100%;
  animation: scrollGlow 2s linear infinite;
  transition: width 0.15s ease;
}
@keyframes scrollGlow {
  0% { background-position: 0% 50%; }
  100% { background-position: 200% 50%; }
}

/* ====== Section ====== */
.sc-section {
  min-height: 100vh;
  display: flex; flex-direction: column; justify-content: center;
  padding: 80px 24px; max-width: 1200px; margin: 0 auto;
}
.sc-section__header { margin-bottom: 48px; }
.sc-section__header--center { text-align: center; }
.sc-section__title {
  font-size: 42px; font-weight: 700; color: #fff;
  margin: 0 0 12px; letter-spacing: -1px;
}
.sc-section__title--gradient {
  background: linear-gradient(135deg, #52C41A, #40A9FF, #1890FF);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-size: 200% 200%;
  animation: textShine 4s ease infinite;
}
@keyframes textShine {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}
.sc-section__sub { font-size: 18px; color: #6B7280; margin: 0; }

/* ====== 滚动揭示动画 ====== */
.reveal-text, .reveal-card, .reveal-up {
  opacity: 0; transform: translateY(30px);
  transition: all 0.7s cubic-bezier(0.4, 0, 0.2, 1);
}
.reveal-text.revealed, .reveal-card.revealed, .reveal-up.revealed {
  opacity: 1; transform: translateY(0);
}
.reveal-card:nth-child(1) { transition-delay: 0s; }
.reveal-card:nth-child(2) { transition-delay: 0.1s; }
.reveal-text:nth-child(1) { transition-delay: 0s; }
.reveal-text:nth-child(2) { transition-delay: 0.08s; }

/* ====== HERO ====== */
.sc-hero {
  position: relative; text-align: center; align-items: center;
  min-height: 110vh;
}
/* 动态极光光晕 */
.sc-hero__aurora {
  position: absolute; inset: -50%;
  pointer-events: none; overflow: hidden;
}
.sc-hero__orb {
  position: absolute; border-radius: 50%; filter: blur(100px);
  animation: orbFloat 15s ease-in-out infinite;
}
.sc-hero__orb--1 {
  width: 700px; height: 700px;
  background: rgba(24,144,255,0.12);
  top: 20%; left: 50%; margin-left: -350px;
  animation-delay: 0s;
}
.sc-hero__orb--2 {
  width: 500px; height: 500px;
  background: rgba(114,46,209,0.08);
  top: 40%; right: 10%;
  animation-delay: -5s;
}
.sc-hero__orb--3 {
  width: 400px; height: 400px;
  background: rgba(82,196,26,0.06);
  bottom: 10%; left: -5%;
  animation-delay: -10s;
}
@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(60px, -40px) scale(1.15); }
  50% { transform: translate(-30px, 30px) scale(0.9); }
  75% { transform: translate(-50px, -20px) scale(1.1); }
}
/* 网格背景 */
.sc-hero__grid {
  position: absolute; inset: 0; pointer-events: none; opacity: 0.03;
  background-image:
    linear-gradient(rgba(255,255,255,0.5) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.5) 1px, transparent 1px);
  background-size: 80px 80px;
  animation: gridMove 20s linear infinite;
}
@keyframes gridMove {
  0% { background-position: 0 0; }
  100% { background-position: 80px 80px; }
}

.sc-hero__content { position: relative; z-index: 1; }

/* 徽章 */
.sc-hero__badge-bar {
  display: inline-flex; align-items: center; gap: 10px;
  background: rgba(24,144,255,0.08);
  border: 1px solid rgba(24,144,255,0.15);
  border-radius: 50px; padding: 6px 20px; margin-bottom: 24px;
}
.sc-hero__badge-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #52C41A;
  box-shadow: 0 0 10px rgba(82,196,26,0.6);
  animation: breathe 2s ease-in-out infinite;
}
@keyframes breathe {
  0%, 100% { opacity: 0.5; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}
.sc-hero__badge {
  font-size: 14px; font-weight: 600; color: #40A9FF;
  letter-spacing: 4px; text-transform: uppercase; margin: 0;
}

/* 标题 */
.sc-hero__title {
  font-size: 64px; font-weight: 800; color: #fff;
  margin: 0 0 20px; line-height: 1.1; letter-spacing: -2px;
  animation: titleSlideIn 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}
@keyframes titleSlideIn {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}
.sc-hero__title--gradient {
  background: linear-gradient(135deg, #40A9FF, #1890FF, #52C41A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-size: 200% 200%;
  animation: textShine 3s ease infinite, titleSlideIn 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}
.sc-hero__desc {
  font-size: 20px; color: #6B7280;
  margin: 0 auto 48px; line-height: 1.6; max-width: 500px;
}

/* 数据亮点 */
.sc-hero__stats {
  display: flex; justify-content: center; gap: 48px;
  margin-bottom: 40px;
}
.sc-hero__stat {
  text-align: center;
  animation: statPopIn 0.6s cubic-bezier(0.4, 0, 0.2, 1) both;
}
.sc-hero__stat:nth-child(1) { animation-delay: 0.2s; }
.sc-hero__stat:nth-child(2) { animation-delay: 0.35s; }
.sc-hero__stat:nth-child(3) { animation-delay: 0.5s; }
@keyframes statPopIn {
  from { opacity: 0; transform: scale(0.8); }
  to { opacity: 1; transform: scale(1); }
}
.sc-hero__stat-num {
  display: block; font-size: 36px; font-weight: 800;
  background: linear-gradient(135deg, #fff, #a0c4ff);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.sc-hero__stat-label {
  font-size: 12px; color: #6B7280; text-transform: uppercase;
  letter-spacing: 1px; margin-top: 4px; display: block;
}

/* 按钮 */
.sc-hero__actions {
  display: flex; gap: 16px; justify-content: center; flex-wrap: wrap;
}
.sc-hero__loading {
  margin-top: 32px; display: flex; align-items: center; gap: 8px;
  color: #6B7280; font-size: 14px; justify-content: center;
}

/* 向下滚动 */
.sc-hero__scroll-hint {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  color: #4B5563; font-size: 12px; animation: floatHint 2s ease-in-out infinite;
}
@keyframes floatHint {
  0%, 100% { opacity: 0.3; transform: translateX(-50%) translateY(0); }
  50% { opacity: 1; transform: translateX(-50%) translateY(8px); }
}
.sc-hero__scroll-mouse {
  width: 24px; height: 38px;
  border: 2px solid rgba(255,255,255,0.2); border-radius: 12px;
  position: relative;
}
.sc-hero__scroll-wheel {
  width: 4px; height: 8px;
  background: rgba(255,255,255,0.4); border-radius: 2px;
  position: absolute; top: 6px; left: 50%; transform: translateX(-50%);
  animation: scrollWheel 1.5s ease-in-out infinite;
}
@keyframes scrollWheel {
  0% { opacity: 1; top: 6px; }
  100% { opacity: 0; top: 22px; }
}

/* ====== 按钮 ====== */
.sc-btn {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 14px 32px; border-radius: 12px; font-size: 16px;
  font-weight: 600; text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.sc-btn--primary {
  background: linear-gradient(135deg, #1890FF, #096DD9);
  color: #fff; box-shadow: 0 4px 20px rgba(24,144,255,0.3);
}
.sc-btn--primary:hover {
  box-shadow: 0 8px 32px rgba(24,144,255,0.5);
  transform: translateY(-2px);
}
.sc-btn--ghost {
  background: rgba(255,255,255,0.04); color: #9CA3AF;
  border: 1px solid rgba(255,255,255,0.08);
}
.sc-btn--ghost:hover {
  background: rgba(255,255,255,0.08); color: #fff;
  border-color: rgba(255,255,255,0.2);
}

/* ====== 卡片 ====== */
.sc-bento {
  display: grid; grid-template-columns: 2fr 1fr; gap: 20px;
}
.sc-card {
  background: rgba(20,20,28,0.8); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 24px; padding: 32px;
  display: flex; flex-direction: column;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.sc-card:hover {
  background: rgba(24,24,34,0.9);
  border-color: rgba(24,144,255,0.2);
  box-shadow: 0 16px 48px rgba(0,0,0,0.3), 0 0 0 1px rgba(24,144,255,0.1);
  transform: translateY(-3px);
}
.sc-card__header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.sc-card__title {
  font-size: 18px; font-weight: 600; color: rgba(255,255,255,0.85); margin: 0;
}
.sc-card__badge {
  font-size: 12px; color: #52C41A;
  background: rgba(82,196,26,0.12); padding: 6px 14px; border-radius: 20px;
  display: flex; align-items: center; gap: 6px;
}

/* 脉冲点 */
.pulse-dot::before {
  content: ''; width: 6px; height: 6px; border-radius: 50%;
  background: #52C41A; display: inline-block;
  animation: statusPulse 2s ease-in-out infinite;
}
@keyframes statusPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(82,196,26,0.4); }
  50% { box-shadow: 0 0 0 6px rgba(82,196,26,0); }
}

/* ====== 客流卡片 ====== */
.sc-traffic__value {
  font-size: 72px; font-weight: 800; color: #fff;
  letter-spacing: -3px; line-height: 1;
}
.sc-traffic__growth {
  display: flex; align-items: center; gap: 6px;
  color: #52C41A; font-size: 14px; margin-top: 8px;
}
.sc-traffic__bars {
  display: flex; gap: 3px; height: 100px;
  align-items: flex-end; margin-top: 24px;
}
.sc-traffic__bar {
  flex: 1;
  background: linear-gradient(180deg, #1890FF, rgba(24,144,255,0.15));
  border-radius: 3px 3px 0 0;
  animation: barRise 1s ease-out both;
}
@keyframes barRise {
  from { height: 0; opacity: 0; }
  to { opacity: 1; }
}

/* ====== 系统状态 ====== */
.sc-status__info {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  flex: 1; gap: 4px; padding: 20px 0;
}

/* ====== 人员卡片 ====== */
.sc-staff__grid {
  display: flex; flex-wrap: wrap; justify-content: center;
  gap: 24px; margin-bottom: 60px;
}
.sc-staff__card {
  width: 240px;
  background: rgba(20,20,28,0.8); backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: 24px; padding: 32px 20px;
  display: flex; flex-direction: column; align-items: center; text-align: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.sc-staff__card:hover {
  transform: translateY(-6px);
  border-color: rgba(24,144,255,0.2);
  box-shadow: 0 20px 40px rgba(0,0,0,0.4), 0 0 0 1px rgba(24,144,255,0.1);
}
.sc-staff__avatar {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #1890FF, #722ED1);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700; color: #fff;
  margin-bottom: 16px;
  box-shadow: 0 8px 25px rgba(24,144,255,0.25);
  transition: transform 0.3s;
}
.sc-staff__card:hover .sc-staff__avatar { transform: scale(1.05); }
.sc-staff__name { font-size: 20px; font-weight: 700; color: #fff; margin: 0 0 4px; }
.sc-staff__role { font-size: 14px; color: #1890FF; font-weight: 500; margin: 0 0 16px; }
.sc-staff__divider {
  width: 100%; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.06), transparent);
  margin-bottom: 12px;
}
.sc-staff__status { font-size: 13px; color: #6B7280; display: flex; align-items: center; gap: 6px; }
.sc-staff__status-dot--active {
  width: 6px; height: 6px; border-radius: 50%;
  background: #52C41A; animation: statusPulse 2s ease-in-out infinite;
}

/* ====== 耗材监测 ====== */
.sc-consumable { max-width: 680px; margin: 0 auto; }
.sc-consumable__title {
  font-size: 22px; font-weight: 700; color: #fff;
  text-align: center; margin: 0 0 24px;
}
/* 进度条 */
.sc-consumable__bar {
  height: 6px; background: rgba(255,255,255,0.04);
  border-radius: 3px; overflow: hidden; margin-bottom: 6px;
}
.sc-consumable__fill {
  height: 100%; background: linear-gradient(90deg, #1890FF, #40A9FF);
  border-radius: 3px; transition: width 1.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.sc-consumable__fill--low { background: linear-gradient(90deg, #FF4D4F, #FF7875); }

/* 耗材分组 */
.sc-consumable__group { margin-bottom: 28px; }
.sc-consumable__group:last-child { margin-bottom: 0; }
.sc-consumable__group-header {
  display: flex; align-items: center; gap: 8px;
  font-size: 15px; font-weight: 600; color: rgba(255,255,255,0.8);
  margin-bottom: 12px; padding-left: 4px;
}
/* 耗材横向卡片 */
.sc-consumable__items {
  display: flex; flex-wrap: wrap; gap: 10px;
}
.sc-consumable__card {
  flex: 1 1 calc(33.33% - 7px); min-width: 160px; max-width: calc(50% - 5px);
  background: rgba(20,20,28,0.6);
  border: 1px solid rgba(255,255,255,0.03);
  border-radius: 14px; padding: 14px;
  transition: all 0.3s;
  display: flex; align-items: center; gap: 12px;
}
.sc-consumable__card:hover { border-color: rgba(255,255,255,0.08); }
.sc-consumable__card-icon {
  width: 40px; height: 40px; min-width: 40px; border-radius: 50%;
  background: rgba(255,255,255,0.06);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 600; color: #fff;
}
.sc-consumable__card-info { flex: 1; min-width: 0; }
.sc-consumable__card-row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13px; color: #9CA3AF; margin-bottom: 6px;
}
.sc-consumable__card-name { font-weight: 600; color: #e0e0e0; }
.sc-consumable__card-pct { color: #1890FF; font-weight: 600; }
.sc-consumable__card-footer {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 12px; color: #6B7280; margin-top: 4px;
}

/* ====== 反馈区 ====== */
.sc-feedback {
  border-top: 1px solid rgba(255,255,255,0.03);
  background: linear-gradient(180deg, #06070a, #0a0c14);
  max-width: 100%; position: relative; overflow: hidden;
}
.sc-feedback__container {
  max-width: 1000px; margin: 0 auto;
  display: flex; gap: 60px; align-items: flex-start; flex-wrap: wrap;
}
.sc-feedback__intro { flex: 0 0 280px; }
.sc-feedback__intro-icon {
  width: 56px; height: 56px; border-radius: 16px;
  background: rgba(24,144,255,0.1);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px; transition: transform 0.3s;
}
.sc-feedback__intro-icon:hover { transform: rotate(-5deg) scale(1.1); }
.sc-feedback__intro h2 { font-size: 28px; font-weight: 700; color: #fff; margin: 0 0 12px; }
.sc-feedback__intro p { font-size: 15px; color: #6B7280; line-height: 1.7; margin: 0; }

.sc-feedback__form { flex: 1; min-width: 320px; }
.sc-feedback__row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }

/* 表单暗色覆盖 */
.sc-feedback__form :deep(.el-select .el-input__wrapper),
.sc-feedback__form :deep(.el-input__wrapper),
.sc-feedback__form :deep(.el-textarea__inner) {
  background: rgba(255,255,255,0.03) !important;
  border: 1px solid rgba(255,255,255,0.06) !important;
  border-radius: 12px !important;
  box-shadow: none !important; color: #fff !important;
  transition: all 0.3s !important;
}
.sc-feedback__form :deep(.el-input__wrapper:hover),
.sc-feedback__form :deep(.el-textarea__inner:hover) {
  border-color: rgba(24,144,255,0.3) !important;
  background: rgba(255,255,255,0.05) !important;
}
.sc-feedback__form :deep(.el-input__wrapper.is-focus),
.sc-feedback__form :deep(.el-textarea__inner:focus) {
  border-color: #1890FF !important;
  box-shadow: 0 0 0 3px rgba(24,144,255,0.1) !important;
}
.sc-feedback__form :deep(.el-form-item__label) { color: #9CA3AF !important; }
.sc-feedback__form :deep(.el-textarea__inner::placeholder),
.sc-feedback__form :deep(.el-input__inner::placeholder) { color: #4B5563 !important; }

/* ====== Footer ====== */
.sc-footer {
  display: flex; justify-content: space-between; align-items: center;
  flex-wrap: wrap; gap: 16px; margin-top: 80px;
  padding: 24px 0; border-top: 1px solid rgba(255,255,255,0.04);
  font-size: 13px; color: #4B5563;
}
.sc-footer__brand { display: flex; align-items: center; gap: 6px; }
.sc-footer__links { display: flex; gap: 24px; }
.sc-footer__links span { cursor: pointer; transition: color 0.2s; }
.sc-footer__links span:hover { color: #9CA3AF; }

/* ====== 响应式 ====== */
@media (max-width: 768px) {
  .sc-section { padding: 60px 16px; }
  .sc-hero__title { font-size: 36px; }
  .sc-hero__stats { gap: 24px; }
  .sc-hero__stat-num { font-size: 28px; }
  .sc-section__title { font-size: 30px; }
  .sc-bento { grid-template-columns: 1fr; }
  .sc-traffic__value { font-size: 48px; }
  .sc-feedback__container { flex-direction: column; }
  .sc-feedback__intro { flex: none; }
  .sc-feedback__row { grid-template-columns: 1fr; }
  .sc-staff__card { width: 100%; max-width: 280px; }
  .sc-consumable__card { flex: 1 1 calc(50% - 5px); min-width: 0; max-width: none; }
}
</style>
