<template>
  <el-drawer v-model="visible" title="站内消息" size="400px" @open="fetchMessages">
    <template #extra>
      <el-button text type="primary" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
        全部已读
      </el-button>
    </template>

    <div v-loading="loading">
      <div v-if="messages.length === 0" style="text-align: center; padding: 60px 0; color: #999">
        <el-icon :size="48" style="color: #d9d9d9"><Message /></el-icon>
        <p style="margin-top: 12px">暂无消息</p>
      </div>

      <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ 'unread': !msg.isRead }"
           @click="handleRead(msg)">
        <div class="message-title">
          <el-tag v-if="!msg.isRead" size="small" type="danger" effect="dark" class="unread-tag">未读</el-tag>
          <span class="title-text">{{ msg.title }}</span>
        </div>
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ msg.createTime }}</div>
      </div>

      <div v-if="page.total > page.size" style="text-align: center; margin-top: 16px">
        <el-button text type="primary" @click="loadMore" :loading="loadingMore">
          加载更多 ({{ messages.length }}/{{ page.total }})
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { getMessagePage, markMessageRead, markAllMessagesRead } from '../api'

const props = defineProps({
  modelValue: Boolean,
  unreadCount: { type: Number, default: 0 }
})
const emit = defineEmits(['update:modelValue', 'update:unreadCount'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, v => visible.value = v)
watch(visible, v => emit('update:modelValue', v))

const messages = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const loading = ref(false)
const loadingMore = ref(false)

async function fetchMessages() {
  loading.value = true
  try {
    const res = await getMessagePage({ current: 1, size: page.size })
    messages.value = res.data.records || []
    page.total = res.data.total || 0
    page.current = 1
  } catch { messages.value = [] } finally { loading.value = false }
}

async function loadMore() {
  loadingMore.value = true
  try {
    page.current++
    const res = await getMessagePage({ current: page.current, size: page.size })
    const records = res.data.records || []
    messages.value.push(...records)
  } finally { loadingMore.value = false }
}

async function handleRead(msg) {
  if (!msg.isRead) {
    await markMessageRead(msg.id).catch(() => {})
    msg.isRead = 1
    emit('update:unreadCount', props.unreadCount - 1)
  }
}

async function handleMarkAllRead() {
  await markAllMessagesRead().catch(() => {})
  messages.value.forEach(m => { m.isRead = 1 })
  emit('update:unreadCount', 0)
}
</script>

<style scoped>
.message-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 8px;
  margin-bottom: 4px;
}
.message-item:hover { background: #f6f8fa; }
.message-item.unread { background: #e6f7ff; }
.message-item.unread:hover { background: #bae7ff; }
.message-title { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.unread-tag { flex-shrink: 0; }
.title-text { font-weight: 600; font-size: 14px; color: #333; }
.message-content { font-size: 13px; color: #666; line-height: 1.5; margin-bottom: 6px; }
.message-time { font-size: 12px; color: #bbb; }
</style>
