<template>
  <div class="exception-manage">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Warning /></el-icon>
            <span>包裹异常管理</span>
          </div>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            登记异常
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="快递单号">
          <el-input v-model="queryForm.trackingNumber" placeholder="请输入快递单号" clearable />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="queryForm.exceptionType" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="破损" value="DAMAGED" />
            <el-option label="丢件" value="LOST" />
            <el-option label="错件" value="WRONG" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNumber" label="订单号" width="140" />
        <el-table-column prop="trackingNumber" label="快递单号" width="160" />
        <el-table-column prop="exceptionType" label="异常类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.exceptionType)" size="small">
              {{ getTypeText(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reporter" label="登记人" width="100" />
        <el-table-column prop="handler" label="处理人" width="100" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="170" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="warning"
              size="small"
              @click="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button
              v-if="row.status === 'PROCESSING'"
              type="success"
              size="small"
              @click="handleCompensate(row)"
            >
              补偿
            </el-button>
            <el-button
              v-if="row.status === 'RESOLVED'"
              type="info"
              size="small"
              @click="handleClose(row)"
            >
              关闭
            </el-button>
            <el-button
              type="primary"
              size="small"
              link
              @click="handleDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="createDialogVisible" title="登记包裹异常" width="550px" destroy-on-close>
      <el-form :model="createForm" label-width="100px" :rules="createRules" ref="createFormRef">
        <el-form-item label="快递单号" prop="trackingNumber">
          <el-input v-model="createForm.trackingNumber" placeholder="请输入快递单号" @blur="handleTrackingNumberBlur" />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="createForm.orderNumber" placeholder="请输入订单号（可选）" />
        </el-form-item>
        <el-form-item label="包裹ID">
          <el-input v-model="createForm.packageId" placeholder="输入单号后自动绑定" disabled />
        </el-form-item>
        <el-form-item v-if="bindPackageInfo" label="包裹信息">
          <div class="bind-info">
            <span>收件人: {{ bindPackageInfo.receiverName }}</span>
            <span>手机号: {{ bindPackageInfo.receiverPhone }}</span>
            <span>货架: {{ bindPackageInfo.shelfLocation || '-' }}</span>
          </div>
        </el-form-item>
        <el-form-item label="异常类型" prop="exceptionType">
          <el-select v-model="createForm.exceptionType" placeholder="请选择异常类型" style="width: 100%" @change="handleExceptionTypeChange">
            <el-option label="破损" value="DAMAGED" />
            <el-option label="丢件" value="LOST" />
            <el-option label="错件" value="WRONG" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="standardAmountHint !== null" label="标准补偿金额">
          <span class="standard-hint">¥{{ standardAmountHint }}（参考补偿标准）</span>
        </el-form-item>
        <el-form-item label="异常描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请描述异常情况" />
        </el-form-item>
        <el-form-item label="登记人" prop="reporter">
          <el-input v-model="createForm.reporter" placeholder="请输入登记人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitLoading">确认登记</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="processDialogVisible" title="处理异常" width="550px" destroy-on-close>
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="异常ID">
          <el-input :model-value="processForm.id" disabled />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input :model-value="processForm.trackingNumber" disabled />
        </el-form-item>
        <el-form-item label="异常描述">
          <el-input :model-value="processForm.description" type="textarea" :rows="2" disabled />
        </el-form-item>
        <el-form-item label="处理人" prop="handler">
          <el-input v-model="processForm.handler" placeholder="请输入处理人姓名" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="processForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="submitLoading">确认处理</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="compensateDialogVisible" title="异常补偿" width="550px" destroy-on-close>
      <el-form :model="compensateForm" label-width="100px" :rules="compensateRules" ref="compensateFormRef">
        <el-form-item label="异常ID">
          <el-input :model-value="compensateForm.exceptionId" disabled />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input :model-value="compensateForm.trackingNumber" disabled />
        </el-form-item>
        <el-form-item label="异常类型">
          <el-tag :type="getTypeTagType(compensateForm.exceptionType)" size="small">
            {{ getTypeText(compensateForm.exceptionType) }}
          </el-tag>
          <span v-if="compensateForm.standardAmount !== null" class="standard-hint" style="margin-left: 12px">
            标准补偿: ¥{{ compensateForm.standardAmount }}
          </span>
        </el-form-item>
        <el-form-item label="补偿金额" prop="compensationAmount">
          <el-input-number v-model="compensateForm.compensationAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="补偿方式" prop="compensationMethod">
          <el-select v-model="compensateForm.compensationMethod" placeholder="请选择补偿方式" style="width: 100%">
            <el-option label="现金退款" value="现金退款" />
            <el-option label="重新发货" value="重新发货" />
            <el-option label="代金券" value="代金券" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="compensateForm.handleRemark" type="textarea" :rows="3" placeholder="请输入补偿备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="compensateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCompensate" :loading="submitLoading">确认补偿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="异常详情" width="600px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="异常ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="快递单号">{{ currentDetail.trackingNumber }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentDetail.orderNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="包裹ID">{{ currentDetail.packageId }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">
          <el-tag :type="getTypeTagType(currentDetail.exceptionType)" size="small">
            {{ getTypeText(currentDetail.exceptionType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusTagType(currentDetail.status)" size="small">
            {{ getStatusText(currentDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="登记人">{{ currentDetail.reporter }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentDetail.handler || '-' }}</el-descriptions-item>
        <el-descriptions-item label="登记时间">{{ currentDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentDetail.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="异常描述" :span="2">{{ currentDetail.description }}</el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ currentDetail.handleRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="补偿金额">{{ currentDetail.compensationAmount != null ? '¥' + currentDetail.compensationAmount : '-' }}</el-descriptions-item>
        <el-descriptions-item label="补偿方式">{{ currentDetail.compensationMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="补偿时间" :span="2">{{ currentDetail.compensationTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchExceptions, createException, updateExceptionStatus, compensate, getCompensationStandardByType, getCompensationStandards } from '@/api/exception'
import { searchPackages } from '@/api/package'

const loading = ref(false)
const submitLoading = ref(false)

const queryForm = reactive({
  trackingNumber: '',
  exceptionType: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const createDialogVisible = ref(false)
const processDialogVisible = ref(false)
const compensateDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const createFormRef = ref(null)
const compensateFormRef = ref(null)

const currentDetail = ref(null)

const bindPackageInfo = ref(null)

const standardAmountHint = ref(null)

const standardMap = ref({})

const createForm = reactive({
  packageId: '',
  trackingNumber: '',
  orderNumber: '',
  exceptionType: '',
  description: '',
  reporter: ''
})

const processForm = reactive({
  id: '',
  trackingNumber: '',
  description: '',
  handler: '',
  handleRemark: ''
})

const compensateForm = reactive({
  exceptionId: '',
  trackingNumber: '',
  exceptionType: '',
  standardAmount: null,
  compensationAmount: 0,
  compensationMethod: '',
  handleRemark: ''
})

const createRules = {
  trackingNumber: [{ required: true, message: '请输入快递单号', trigger: 'blur' }],
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入异常描述', trigger: 'blur' }],
  reporter: [{ required: true, message: '请输入登记人', trigger: 'blur' }]
}

const compensateRules = {
  compensationAmount: [{ required: true, message: '请输入补偿金额', trigger: 'blur' }],
  compensationMethod: [{ required: true, message: '请选择补偿方式', trigger: 'change' }]
}

const getTypeText = (type) => {
  const map = { 'DAMAGED': '破损', 'LOST': '丢件', 'WRONG': '错件' }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = { 'DAMAGED': 'warning', 'LOST': 'danger', 'WRONG': 'info' }
  return map[type] || 'info'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待处理', 'PROCESSING': '处理中', 'RESOLVED': '已解决', 'CLOSED': '已关闭' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { 'PENDING': 'danger', 'PROCESSING': 'warning', 'RESOLVED': 'success', 'CLOSED': 'info' }
  return map[status] || 'info'
}

const loadStandards = async () => {
  try {
    const res = await getCompensationStandards()
    if (Array.isArray(res)) {
      const map = {}
      res.forEach(s => {
        if (s.enabled) {
          map[s.exceptionType] = s.standardAmount
        }
      })
      standardMap.value = map
    }
  } catch (e) {}
}

const handleTrackingNumberBlur = async () => {
  bindPackageInfo.value = null
  createForm.packageId = ''
  if (!createForm.trackingNumber) return
  try {
    const res = await searchPackages({ trackingNumber: createForm.trackingNumber })
    if (Array.isArray(res) && res.length > 0) {
      const pkg = res[0]
      createForm.packageId = pkg.id
      bindPackageInfo.value = {
        receiverName: pkg.receiverName,
        receiverPhone: pkg.receiverPhone,
        shelfLocation: pkg.shelfLocation
      }
    }
  } catch (e) {}
}

const handleExceptionTypeChange = (val) => {
  if (val && standardMap.value[val] !== undefined) {
    standardAmountHint.value = standardMap.value[val]
  } else {
    standardAmountHint.value = null
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (queryForm.trackingNumber) params.trackingNumber = queryForm.trackingNumber
    if (queryForm.exceptionType) params.exceptionType = queryForm.exceptionType
    if (queryForm.status) params.status = queryForm.status

    const res = await searchExceptions(params)
    if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (e) {
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.trackingNumber = ''
  queryForm.exceptionType = ''
  queryForm.status = ''
  handleQuery()
}

const handleCreate = () => {
  Object.assign(createForm, {
    packageId: '',
    trackingNumber: '',
    orderNumber: '',
    exceptionType: '',
    description: '',
    reporter: ''
  })
  bindPackageInfo.value = null
  standardAmountHint.value = null
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  submitLoading.value = true
  try {
    const data = { ...createForm }
    if (data.packageId) data.packageId = Number(data.packageId)
    else delete data.packageId
    await createException(data)
    ElMessage.success('异常登记成功')
    createDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('登记失败: ' + (e.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

const handleProcess = (row) => {
  Object.assign(processForm, {
    id: row.id,
    trackingNumber: row.trackingNumber,
    description: row.description,
    handler: '',
    handleRemark: ''
  })
  processDialogVisible.value = true
}

const submitProcess = async () => {
  submitLoading.value = true
  try {
    await updateExceptionStatus(processForm.id, {
      status: 'PROCESSING',
      handler: processForm.handler,
      handleRemark: processForm.handleRemark
    })
    ElMessage.success('已开始处理')
    processDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败: ' + (e.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

const handleCompensate = (row) => {
  const stdAmount = standardMap.value[row.exceptionType] !== undefined ? standardMap.value[row.exceptionType] : null
  Object.assign(compensateForm, {
    exceptionId: row.id,
    trackingNumber: row.trackingNumber,
    exceptionType: row.exceptionType,
    standardAmount: stdAmount,
    compensationAmount: stdAmount !== null ? stdAmount : 0,
    compensationMethod: '',
    handleRemark: ''
  })
  compensateDialogVisible.value = true
}

const submitCompensate = async () => {
  if (!compensateFormRef.value) return
  await compensateFormRef.value.validate()
  submitLoading.value = true
  try {
    await compensate({
      exceptionId: compensateForm.exceptionId,
      compensationAmount: compensateForm.compensationAmount,
      compensationMethod: compensateForm.compensationMethod,
      handleRemark: compensateForm.handleRemark
    })
    ElMessage.success('补偿成功')
    compensateDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('补偿失败: ' + (e.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确认关闭该异常记录？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateExceptionStatus(row.id, { status: 'CLOSED' })
    ElMessage.success('已关闭')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDetail = (row) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
  loadStandards()
})
</script>

<style scoped>
.exception-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.query-form {
  margin-bottom: 20px;
}

.bind-info {
  display: flex;
  gap: 16px;
  color: #606266;
  font-size: 13px;
}

.standard-hint {
  color: #e6a23c;
  font-weight: 500;
  font-size: 13px;
}
</style>
