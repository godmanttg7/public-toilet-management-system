import request from '../utils/request'

// ==================== 认证 ====================
export const login = (data) => request.post('/auth/login', data)
export const getUserInfo = () => request.get('/auth/info')

// ==================== 公厕管理 ====================
export const getToiletPage = (params) => request.get('/toilet/page', { params })
export const getToiletList = () => request.get('/toilet/list')
export const getToiletById = (id) => request.get(`/toilet/${id}`)
export const addToilet = (data) => request.post('/toilet', data)
export const updateToilet = (data) => request.put('/toilet', data)
export const deleteToilet = (id) => request.delete(`/toilet/${id}`)

// ==================== 用户管理 ====================
export const getUserPage = (params) => request.get('/user/page', { params })
export const getUserList = (role) => request.get('/user/list', { params: { role } })
export const addUser = (data) => request.post('/user', data)
export const updateUser = (data) => request.put('/user', data)
export const deleteUser = (id) => request.delete(`/user/${id}`)
export const resetPassword = (id) => request.put(`/user/reset-pwd/${id}`)

// ==================== 设施管理 ====================
export const getFacilityPage = (params) => request.get('/facility/page', { params })
export const getFacilityList = (params) => request.get('/facility/list', { params })
export const addFacility = (data) => request.post('/facility', data)
export const updateFacility = (data) => request.put('/facility', data)
export const deleteFacility = (id) => request.delete(`/facility/${id}`)

// ==================== 维修工单 ====================
export const getRepairOrderPage = (params) => request.get('/repair/page', { params })
export const getMyRepairOrders = (params) => request.get('/repair/my', { params })
export const reportRepair = (data) => request.post('/repair', data)
export const assignRepair = (data) => request.put('/repair/assign', data)
export const updateRepairStatus = (data) => request.put('/repair/status', data)
export const cancelRepair = (id) => request.put(`/repair/cancel/${id}`)
export const deleteRepairOrder = (id) => request.delete(`/repair/${id}`)

// ==================== 保洁排班 ====================
export const getSchedulePage = (params) => request.get('/schedule/page', { params })
export const getMySchedules = (params) => request.get('/schedule/my', { params })
export const addSchedule = (data) => request.post('/schedule', data)
export const deleteSchedule = (id) => request.delete(`/schedule/${id}`)
export const checkin = (id) => request.put(`/schedule/checkin/${id}`)
export const signoff = (id) => request.put(`/schedule/signoff/${id}`)
export const scoreSchedule = (data) => request.put('/schedule/score', data)

// ==================== 耗材管理 ====================
export const getConsumablePage = (params) => request.get('/consumable/page', { params })
export const getConsumableAlerts = () => request.get('/consumable/alerts')
export const stockIn = (data) => request.post('/consumable/in', data)
export const stockOut = (data) => request.post('/consumable/out', data)
export const getConsumableNames = () => request.get('/consumable/names')
export const getConsumableRecordPage = (params) => request.get('/consumable/record/page', { params })

// ==================== 公众反馈 ====================
export const getFeedbackPage = (params) => request.get('/feedback/page', { params })
export const submitFeedback = (data) => request.post('/feedback', data)

// ==================== 数据分析 ====================
export const getDashboardStats = () => request.get('/dashboard/stats')
export const getCleanRate = () => request.get('/dashboard/clean-rate')
export const getFacilityRate = () => request.get('/dashboard/facility-rate')
export const getSatisfaction = () => request.get('/dashboard/satisfaction')
export const getConsumableUsage = () => request.get('/dashboard/consumable-usage')
export const getDashboardAlerts = () => request.get('/dashboard/alerts')

// ==================== 特殊情况汇报 ====================
export const getIncidentPage = (params) => request.get('/incident/page', { params })
export const getMyIncidents = (params) => request.get('/incident/my', { params })
export const reportIncident = (data) => request.post('/incident', data)
export const resolveIncident = (id) => request.put(`/incident/resolve/${id}`)
export const getUnresolvedIncidentCount = () => request.get('/incident/unresolved-count')

// ==================== 文件上传 ====================
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ==================== 站内消息 ====================
export const getMessagePage = (params) => request.get('/message/page', { params })
export const getUnreadMessageCount = () => request.get('/message/unread-count')
export const markMessageRead = (id) => request.put(`/message/read/${id}`)
export const markAllMessagesRead = () => request.put('/message/read-all')

// ==================== 公众端 ====================
export const getPublicToilets = (params) => request.get('/public/toilets', { params })
export const getPublicToiletDetail = (id) => request.get(`/public/toilet/${id}`)
export const submitPublicFeedback = (data) => request.post('/public/feedback', data)
