<template>
  <div class="feedback-container">
    <!-- 顶部返回栏 -->
    <div class="feedback-topbar">
      <el-button text @click="$router.push('/public')" style="color: #fff; font-size: 15px">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回公厕列表</span>
      </el-button>
    </div>

    <el-card class="feedback-card">
      <template #header>
        <div style="text-align: center; font-size: 18px; font-weight: bold">
          公厕满意度评价
        </div>
      </template>

      <div v-if="submitted" style="text-align: center; padding: 30px">
        <el-icon :size="60" color="#67C23A"><CircleCheckFilled /></el-icon>
        <div style="font-size: 18px; color: #67C23A; margin-top: 15px">评价提交成功！</div>
        <div style="color: #999; margin-top: 10px">感谢您的反馈，我们会继续改进服务质量</div>
        <el-button type="primary" style="margin-top: 20px" @click="$router.push('/public')">
          返回公厕列表
        </el-button>
      </div>

      <div v-else>
        <el-form label-width="0">
          <!-- 公厕选择 -->
          <el-form-item v-if="toiletList.length > 0">
            <el-select v-model="selectedToiletId" placeholder="请选择要评价的公厕" filterable style="width: 100%"
              :disabled="!!route.params.toiletId">
              <el-option v-for="t in toiletList" :key="t.id"
                :label="t.name + ' - ' + t.address" :value="t.id" />
            </el-select>
          </el-form-item>

          <el-divider />

          <div style="text-align: center; margin-bottom: 30px">
            <div style="font-size: 14px; color: #666; margin-bottom: 15px">
              请为{{ toilet?.name || '该公厕' }}的服务质量打分
            </div>
            <el-rate
              v-model="form.score"
              :max="5"
              :texts="['很差', '较差', '一般', '满意', '非常满意']"
              show-text
              size="large"
              style="justify-content: center"
            />
          </div>

          <el-form-item>
            <el-input v-model="form.content" type="textarea" :rows="4"
              placeholder="请留下您的宝贵意见（选填）" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item style="text-align: center">
            <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
              提交评价
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 公厕信息 -->
        <el-divider />
        <div v-if="toilet" style="font-size: 13px; color: #666">
          <div><strong>地址：</strong>{{ toilet.address }}</div>
          <div style="margin-top: 5px"><strong>开放时间：</strong>{{ toilet.openTime || '未设置' }}</div>
          <div style="margin-top: 5px">
            <strong>厕位：</strong>
            男{{ toilet.maleCount }}个 / 女{{ toilet.femaleCount }}个 / 无障碍{{ toilet.accessibleCount }}个
          </div>
        </div>
        <div v-else style="font-size: 13px; color: #999; text-align: center">
          请在上方选择一个公厕进行评价
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicToilets, getPublicToiletDetail, submitPublicFeedback } from '../../api'

const route = useRoute()
const toilet = ref(null)
const toiletList = ref([])
const selectedToiletId = ref(null)
const submitted = ref(false)
const submitting = ref(false)

const form = reactive({
  score: 3,
  content: ''
})

watch(selectedToiletId, async (newVal) => {
  if (!newVal) { toilet.value = null; return }
  try {
    const res = await getPublicToiletDetail(newVal)
    toilet.value = res.data
  } catch { toilet.value = null }
})

async function handleSubmit() {
  if (form.score === 0) {
    ElMessage.warning('请先进行评分')
    return
  }
  const toiletId = selectedToiletId.value
  if (!toiletId) {
    ElMessage.warning('请先选择要评价的公厕')
    return
  }
  submitting.value = true
  try {
    await submitPublicFeedback({
      toiletId,
      score: form.score,
      content: form.content
    })
    submitted.value = true
  } finally { submitting.value = false }
}

onMounted(async () => {
  // 如果从扫码/链接进入，预选公厕
  if (route.params.toiletId) {
    selectedToiletId.value = Number(route.params.toiletId)
  }
  // 加载公厕列表供选择
  try {
    const res = await getPublicToilets()
    toiletList.value = res.data || []
  } catch { toiletList.value = [] }
})
</script>

<style scoped>
.feedback-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
.feedback-card {
  width: 500px;
}
.feedback-topbar {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 100;
  padding: 12px 20px;
  background: rgba(102, 126, 234, 0.9);
  backdrop-filter: blur(10px);
}

</style>
