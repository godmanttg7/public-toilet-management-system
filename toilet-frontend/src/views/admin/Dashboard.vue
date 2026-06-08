<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div
        v-for="card in statCards"
        :key="card.label"
        class="stat-card"
        :class="{ 'stat-card--loading': loading }"
        @click="card.click"
      >
        <div class="stat-card__body">
          <div class="stat-card__info">
            <span class="stat-card__label">{{ card.label }}</span>
            <span class="stat-card__value" :style="{ color: card.color }">
              <template v-if="loading">
                <span class="skeleton skeleton--text"></span>
              </template>
              <template v-else>
                {{ card.value }}
              </template>
            </span>
            <span class="stat-card__trend">
              {{ card.trend }}
            </span>
          </div>
          <div class="stat-card__icon" :style="{ background: card.iconBg }">
            <el-icon :size="28" :color="card.color">
              <component :is="card.icon" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="chart-card" v-for="chart in charts" :key="chart.title">
        <div class="chart-card__header">
          <h3 class="chart-card__title">{{ chart.title }}</h3>
          <span class="chart-card__subtitle">{{ chart.subtitle }}</span>
        </div>
        <div class="chart-card__body">
          <div
            :ref="(el) => (chartRefs[chart.key] = el)"
            class="chart-card__chart"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  getDashboardStats, getCleanRate, getFacilityRate,
  getSatisfaction, getConsumableUsage
} from '../../api'

const loading = ref(true)
const chartRefs = reactive({})
const router = useRouter()

const statCards = reactive([
  {
    label: '公厕总数',
    value: 0,
    trend: '管理点位',
    icon: 'Location',
    color: '#1890FF',
    iconBg: 'rgba(24,144,255,0.1)',
    click: () => router.push('/admin/toilet')
  },
  {
    label: '用户总数',
    value: 0,
    trend: '系统用户',
    icon: 'User',
    color: '#52C41A',
    iconBg: 'rgba(82,196,26,0.1)',
    click: () => router.push('/admin/user')
  },
  {
    label: '待处理工单',
    value: 0,
    trend: '需关注',
    icon: 'Tools',
    color: '#FAAD14',
    iconBg: 'rgba(250,173,20,0.1)',
    click: () => router.push('/admin/repair')
  },
  {
    label: '库存预警',
    value: 0,
    trend: '低库存项目',
    icon: 'Warning',
    color: '#FF4D4F',
    iconBg: 'rgba(255,77,79,0.1)',
    click: () => router.push('/admin/consumable')
  }
])

const charts = [
  { key: 'facility', title: '设施完好率', subtitle: '各公厕设施状态统计 (%)', type: 'bar' },
  { key: 'satisfaction', title: '公众满意度', subtitle: '近30天评分分布', type: 'bar' },
  { key: 'clean', title: '保洁合格率', subtitle: '近30天保洁评分趋势 (%)', type: 'line' },
  { key: 'consumable', title: '耗材使用量', subtitle: '近30天消耗趋势', type: 'bar' }
]

const chartInstances = {}

// ECharts 统一主题配置
function getBaseOption(title, data, type = 'bar') {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: { color: '#1F2937', fontSize: 13 },
      boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
      axisPointer: {
        type: 'shadow',
        shadowStyle: { color: 'rgba(24,144,255,0.03)' }
      }
    },
    grid: {
      left: 48,
      right: 24,
      top: 20,
      bottom: type === 'line' ? 36 : 28
    },
    xAxis: {
      type: 'category',
      data: (data || []).map(d => d.name),
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisTick: { show: false },
      axisLabel: {
        color: '#6B7280',
        fontSize: 11,
        rotate: data && data.length > 6 ? 30 : 0
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
      axisLabel: { color: '#9CA3AF', fontSize: 11 }
    },
    series: [{
      data: (data || []).map(d => d.value),
      type: type,
      barMaxWidth: 36,
      smooth: type === 'line',
      symbol: type === 'line' ? 'circle' : undefined,
      symbolSize: 6,
      lineStyle: type === 'line' ? { width: 3 } : undefined,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#1890FF' },
          { offset: 1, color: 'rgba(24,144,255,0.3)' }
        ])
      },
      emphasis: {
        itemStyle: { color: '#1890FF' }
      }
    }]
  }
}

function createChart(key, title, data, type = 'bar') {
  const el = chartRefs[key]
  if (!el) return
  if (chartInstances[key]) chartInstances[key].dispose()
  const instance = echarts.init(el)
  instance.setOption(getBaseOption(title, data, type))
  chartInstances[key] = instance
}

async function loadData() {
  loading.value = true
  try {
    const stats = await getDashboardStats()
    if (stats.data) {
      statCards[0].value = stats.data.totalToilets || 0
      statCards[1].value = stats.data.totalUsers || 0
      statCards[2].value = stats.data.pendingOrders || 0
      statCards[3].value = stats.data.lowStockCount || 0
    }

    const [facilityData, satisfactionData, cleanData, consumableData] =
      await Promise.allSettled([
        getFacilityRate(), getSatisfaction(), getCleanRate(), getConsumableUsage()
      ])

    await nextTick()

    if (facilityData.value?.data) createChart('facility', '设施完好率', facilityData.value.data)
    if (satisfactionData.value?.data) createChart('satisfaction', '满意度', satisfactionData.value.data)
    if (cleanData.value?.data) createChart('clean', '保洁合格率', cleanData.value.data, 'line')
    if (consumableData.value?.data) createChart('consumable', '耗材使用量', consumableData.value.data)
  } catch (e) {
    console.error('加载统计数据失败', e)
  } finally {
    loading.value = false
  }
}

function handleResize() {
  Object.values(chartInstances).forEach(c => c?.resize())
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(c => c.dispose())
})
</script>

<style scoped>
.dashboard { max-width: 1400px; }

/* 统计卡片网格 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
  cursor: pointer;
}
.stat-card:hover {
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.stat-card__body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.stat-card__info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-card__label {
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
}

.stat-card__value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -1px;
}

.stat-card__trend {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 2px;
}

.stat-card__icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 图表网格 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  overflow: hidden;
}

.chart-card__header {
  padding: 20px 24px 0;
}

.chart-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #1F2937;
  margin: 0;
}

.chart-card__subtitle {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 4px;
  display: block;
}

.chart-card__body {
  padding: 16px 24px 20px;
}

.chart-card__chart {
  height: 300px;
  width: 100%;
}

/* Loading skeleton */
.skeleton--text {
  display: inline-block;
  width: 60px;
  height: 28px;
}

.stat-card--loading .stat-card__value {
  min-width: 60px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .stat-cards { grid-template-columns: 1fr; }
}
</style>
