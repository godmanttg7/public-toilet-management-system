<template>
  <div class="public-page">
    <!-- 顶部导航栏 -->
    <header class="public-header">
      <div class="header-left">
        <router-link to="/" class="header-back">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回首页</span>
        </router-link>
        <div class="header-title">公厕查询</div>
      </div>
      <div class="header-right">
        <el-input v-model="keyword" placeholder="搜索公厕名称或地址" clearable
          prefix-icon="Search" @change="fetchToilets" class="search-input" />
        <el-select v-model="district" placeholder="区域" clearable
          @change="fetchToilets" class="district-select">
          <el-option v-for="d in districts" :key="d" :label="d" :value="d" />
        </el-select>
      </div>
    </header>

    <main class="public-main">
      <!-- 统计区 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-value">{{ toilets.length }}</div>
          <div class="stat-label">公厕总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ toilets.reduce((s, t) => s + t.maleCount + t.femaleCount + t.accessibleCount, 0) }}</div>
          <div class="stat-label">总厕位数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ toilets.filter(t => t.status === 1).length }}</div>
          <div class="stat-label">运营中</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ new Set(toilets.map(t => t.district).filter(Boolean)).size }}</div>
          <div class="stat-label">区域覆盖</div>
        </div>
      </div>

      <!-- 公厕列表 -->
      <div class="toilet-grid">
        <div v-for="toilet in toilets" :key="toilet.id" class="toilet-card">
          <div class="card-top">
            <div class="card-name">{{ toilet.name }}</div>
            <el-tag :type="toilet.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
              {{ toilet.status === 1 ? '运营中' : '停用' }}
            </el-tag>
          </div>
          <div class="card-address">
            <el-icon><Location /></el-icon>
            {{ toilet.address }}
          </div>
          <div class="card-info">
            <div class="info-item">
              <span class="info-label">男厕位</span>
              <span class="info-value">{{ toilet.maleCount }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">女厕位</span>
              <span class="info-value">{{ toilet.femaleCount }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">无障碍</span>
              <span class="info-value">{{ toilet.accessibleCount }}</span>
            </div>
          </div>
          <div class="card-footer">
            <span class="card-time">
              <el-icon><Clock /></el-icon>
              {{ toilet.openTime || '未设置' }}
            </span>
            <el-button type="primary" size="small" round @click="goFeedback(toilet.id)">
              评价
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="toilets.length === 0 && !loading" description="暂无公厕数据" />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublicToilets } from '../../api'

const router = useRouter()
const keyword = ref('')
const district = ref('')
const toilets = ref([])
const loading = ref(false)

const districts = ref([])

async function fetchToilets() {
  loading.value = true
  try {
    const res = await getPublicToilets({ keyword: keyword.value, district: district.value })
    toilets.value = res.data || []
    // 从返回数据中提取不重复的区域列表，过滤掉无效值
    const validDistricts = [...new Set(toilets.value.map(t => t.district).filter(Boolean))]
    districts.value = validDistricts.sort()
  } finally { loading.value = false }
}

function goFeedback(toiletId) {
  router.push(`/public/feedback/${toiletId}`)
}

onMounted(() => fetchToilets())
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  background: #06070a;
  color: #fff;
}

/* ====== 顶部导航 ====== */
.public-header {
  background: rgba(20,20,28,0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255,255,255,0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 30px;
  position: sticky;
  top: 0;
  z-index: 10;
  flex-wrap: wrap;
  gap: 12px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-back {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #40A9FF;
  text-decoration: none;
  font-size: 14px;
  transition: opacity 0.2s;
}
.header-back:hover { opacity: 0.7; }
.header-title {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}
.header-right {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.search-input { width: 240px; }
.district-select { width: 140px; }
.header-right :deep(.el-input__wrapper),
.header-right :deep(.el-select .el-input__wrapper) {
  background: rgba(255,255,255,0.04) !important;
  border: 1px solid rgba(255,255,255,0.08) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  color: #fff !important;
}
.header-right :deep(.el-input__inner) { color: #fff !important; }
.header-right :deep(.el-input__inner::placeholder) { color: #4B5563 !important; }
.header-right :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(24,144,255,0.3) !important;
}

/* ====== 主体 ====== */
.public-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ====== 统计行 ====== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.stat-card {
  background: rgba(20,20,28,0.8);
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
}
.stat-card:hover {
  border-color: rgba(24,144,255,0.2);
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
  transform: translateY(-2px);
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #fff, #a0c4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.stat-label { font-size: 13px; color: #6B7280; margin-top: 4px; }

/* ====== 公厕卡片网格 ====== */
.toilet-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.toilet-card {
  background: rgba(20,20,28,0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: 20px;
  padding: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.toilet-card:hover {
  border-color: rgba(24,144,255,0.2);
  box-shadow: 0 12px 32px rgba(0,0,0,0.3), 0 0 0 1px rgba(24,144,255,0.08);
  transform: translateY(-3px);
}

/* ====== 卡片内容 ====== */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}
.card-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
}
.card-address {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  font-size: 13px;
  color: #9CA3AF;
  margin-bottom: 14px;
  line-height: 1.5;
}
.card-info {
  display: flex;
  gap: 0;
  background: rgba(255,255,255,0.03);
  border-radius: 12px;
  margin-bottom: 14px;
  overflow: hidden;
}
.info-item {
  flex: 1;
  text-align: center;
  padding: 10px 8px;
  border-right: 1px solid rgba(255,255,255,0.04);
}
.info-item:last-child { border-right: none; }
.info-label { display: block; font-size: 11px; color: #6B7280; margin-bottom: 4px; }
.info-value { display: block; font-size: 18px; font-weight: 700; color: #40A9FF; }
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6B7280;
}

/* ====== 响应式 ====== */
@media (max-width: 900px) {
  .toilet-grid { grid-template-columns: repeat(2, 1fr); }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .toilet-grid { grid-template-columns: 1fr; }
  .public-header { flex-direction: column; align-items: stretch; }
  .header-right { flex-direction: column; }
  .search-input, .district-select { width: 100% !important; }
}
</style>
