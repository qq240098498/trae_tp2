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
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderId" placeholder="请输入订单号" clearable />
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
            <el-option label="待审批" value="APPROVAL_PENDING" />
            <el-option label="审批通过" value="APPROVED" />
            <el-option label="审批驳回" value="REJECTED" />
            <el-option label="待打款" value="PAYMENT_PENDING" />
            <el-option label="已打款" value="PAID" />
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
        <el-table-column prop="orderId" label="订单号" width="140" />
        <el-table-column prop="trackingNumber" label="快递单号" width="160" />
        <el-table-column prop="packageId" label="包裹ID" width="80" />
        <el-table-column prop="exceptionType" label="异常类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.exceptionType)" size="small">
              {{ getTypeText(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="reporter" label="登记人" width="80" />
        <el-table-column prop="handler" label="处理人" width="80" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="compensationAmount" label="补偿金额" width="100">
          <template #default="{ row }">
            <span v-if="row.compensationAmount != null" class="amount">¥{{ row.compensationAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
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
              v-if="row.status === 'APPROVAL_PENDING'"
              type="primary"
              size="small"
              @click="handleApprove(row)"
            >
              审批
            </el-button>
            <el-button
              v-if="row.status === 'APPROVED'"
              type="success"
              size="small"
              @click="handlePayment(row)"
            >
              打款
            </el-button>
            <el-button
              v-if="row.status === 'PAID'"
              type="info"
              size="small"
              @click="handleClose(row)"
            >
              关闭
            </el-button>
            <el-button
              v-if="row.status === 'REJECTED'"
              type="warning"
              size="small"
              @click="handleReprocess(row)"
            >
              重新处理
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
        <el-form-item label="包裹ID" prop="packageId">
          <el-input v-model="createForm.packageId" placeholder="输入单号后自动绑定" disabled />
        </el-form-item>
        <el-form-item v-if="bindPackageInfo" label="包裹信息">
          <div class="bind-info bind-success">
            <span>收件人: {{ bindPackageInfo.receiverName }}</span>
            <span>手机号: {{ bindPackageInfo.receiverPhone }}</span>
            <span>货架: {{ bindPackageInfo.shelfLocation || '-' }}</span>
          </div>
        </el-form-item>
        <el-form-item v-if="createForm.trackingNumber && !createForm.packageId" label="包裹信息">
          <div class="bind-info bind-fail">
            <el-icon><WarningFilled /></el-icon>
            <span>未找到该快递单号对应的入库包裹</span>
          </div>
        </el-form-item>
        <el-form-item label="订单号" prop="orderId">
          <el-input v-model="createForm.orderId" placeholder="请输入订单号（必填）" />
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
        <el-form-item label="订单号">
          <el-input :model-value="processForm.orderId" disabled />
        </el-form-item>
        <el-form-item label="包裹ID">
          <el-input :model-value="processForm.packageId" disabled />
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
        <el-form-item label="订单号">
          <el-input :model-value="compensateForm.orderId" disabled />
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
        <el-button type="primary" @click="submitCompensate" :loading="submitLoading">确认补偿（提交审批）</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveDialogVisible" title="审批补偿" width="550px" destroy-on-close>
      <el-form :model="approveForm" label-width="100px" :rules="approveRules" ref="approveFormRef">
        <el-form-item label="异常ID">
          <el-input :model-value="approveForm.exceptionId" disabled />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input :model-value="approveForm.trackingNumber" disabled />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input :model-value="approveForm.orderId" disabled />
        </el-form-item>
        <el-form-item label="补偿金额">
          <span class="amount">¥{{ approveForm.compensationAmount }}</span>
        </el-form-item>
        <el-form-item label="补偿方式">
          <el-tag type="success" size="small">{{ approveForm.compensationMethod }}</el-tag>
        </el-form-item>
        <el-form-item label="审批人" prop="approver">
          <el-input v-model="approveForm.approver" placeholder="请输入审批人姓名" />
        </el-form-item>
        <el-form-item label="审批结果" prop="approved">
          <el-radio-group v-model="approveForm.approved">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批备注">
          <el-input v-model="approveForm.approvalRemark" type="textarea" :rows="3" placeholder="请输入审批备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitApprove(false)" :loading="submitLoading">驳回</el-button>
        <el-button type="success" @click="submitApprove(true)" :loading="submitLoading">审批通过</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="paymentDialogVisible" title="确认打款" width="550px" destroy-on-close>
      <el-form :model="paymentForm" label-width="100px" :rules="paymentRules" ref="paymentFormRef">
        <el-form-item label="异常ID">
          <el-input :model-value="paymentForm.exceptionId" disabled />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input :model-value="paymentForm.trackingNumber" disabled />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input :model-value="paymentForm.orderId" disabled />
        </el-form-item>
        <el-form-item label="补偿金额">
          <span class="amount">¥{{ paymentForm.compensationAmount }}</span>
        </el-form-item>
        <el-form-item label="补偿方式">
          <el-tag type="success" size="small">{{ paymentForm.compensationMethod }}</el-tag>
        </el-form-item>
        <el-form-item label="审批人">
          {{ paymentForm.approver }}
        </el-form-item>
        <el-form-item label="审批时间">
          {{ paymentForm.approvalTime }}
        </el-form-item>
        <el-form-item label="打款操作人" prop="paymentOperator">
          <el-input v-model="paymentForm.paymentOperator" placeholder="请输入打款操作人姓名" />
        </el-form-item>
        <el-form-item label="打款备注">
          <el-input v-model="paymentForm.paymentRemark" type="textarea" :rows="3" placeholder="请输入打款备注（如打款流水号等）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitPayment" :loading="submitLoading">确认打款</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="异常详情" width="650px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="异常ID">{{ currentDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="快递单号">{{ currentDetail.trackingNumber }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentDetail.orderId || '-' }}</el-descriptions-item>
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
        <el-descriptions-item label="补偿时间">{{ currentDetail.compensationTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentDetail.approver || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentDetail.approvalTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批备注">{{ currentDetail.approvalRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="打款操作人">{{ currentDetail.paymentOperator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="打款时间">{{ currentDetail.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="打款备注" :span="2">{{ currentDetail.paymentRemark || '-' }}</el-descriptions-item>
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
import { searchExceptions, createException, updateExceptionStatus, compensate, approve, payment, getCompensationStandards } from '@/api/exception'
import { searchPackages } from '@/api/package'

const loading = ref(false)
const submitLoading = ref(false)

const queryForm = reactive({
  trackingNumber: '',
  orderId: '',
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
const approveDialogVisible = ref(false)
const paymentDialogVisible = ref(false)
const detailDialogVisible = ref(false)

const createFormRef = ref(null)
const compensateFormRef = ref(null)
const approveFormRef = ref(null)
const paymentFormRef = ref(null)

const currentDetail = ref(null)

const bindPackageInfo = ref(null)

const standardAmountHint = ref(null)

const standardMap = ref({})

const createForm = reactive({
  packageId: '',
  trackingNumber: '',
  orderId: '',
  exceptionType: '',
  description: '',
  reporter: ''
})

const processForm = reactive({
  id: '',
  trackingNumber: '',
  orderId: '',
  packageId: '',
  description: '',
  handler: '',
  handleRemark: ''
})

const compensateForm = reactive({
  exceptionId: '',
  trackingNumber: '',
  orderId: '',
  exceptionType: '',
  standardAmount: null,
  compensationAmount: 0,
  compensationMethod: '',
  handleRemark: ''
})

const approveForm = reactive({
  exceptionId: '',
  trackingNumber: '',
  orderId: '',
  compensationAmount: null,
  compensationMethod: '',
  approver: '',
  approved: true,
  approvalRemark: ''
})

const paymentForm = reactive({
  exceptionId: '',
  trackingNumber: '',
  orderId: '',
  compensationAmount: null,
  compensationMethod: '',
  approver: '',
  approvalTime: '',
  paymentOperator: '',
  paymentRemark: ''
})

const validatePackageBind = (rule, value, callback) => {
  if (!createForm.packageId) {
    callback(new Error('未找到对应入库包裹，请确认快递单号'))
  } else {
    callback()
  }
}

const createRules = {
  trackingNumber: [
    { required: true, message: '请输入快递单号', trigger: 'blur' },
    { validator: validatePackageBind, trigger: 'blur' }
  ],
  orderId: [{ required: true, message: '订单号不能为空，异常登记必须绑定订单', trigger: 'blur' }],
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入异常描述', trigger: 'blur' }],
  reporter: [{ required: true, message: '请输入登记人', trigger: 'blur' }]
}

const compensateRules = {
  compensationAmount: [{ required: true, message: '请输入补偿金额', trigger: 'blur' }],
  compensationMethod: [{ required: true, message: '请选择补偿方式', trigger: 'change' }]
}

const approveRules = {
  approver: [{ required: true, message: '请输入审批人', trigger: 'blur' }],
  approved: [{ required: true, message: '请选择审批结果', trigger: 'change' }]
}

const paymentRules = {
  paymentOperator: [{ required: true, message: '请输入打款操作人', trigger: 'blur' }]
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
  const map = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPENSATION_PENDING': '待补偿',
    'APPROVAL_PENDING': '待审批',
    'APPROVED': '审批通过',
    'REJECTED': '审批驳回',
    'PAYMENT_PENDING': '待打款',
    'PAID': '已打款',
    'CLOSED': '已关闭'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    'PENDING': 'danger',
    'PROCESSING': 'warning',
    'COMPENSATION_PENDING': 'warning',
    'APPROVAL_PENDING': '',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PAYMENT_PENDING': '',
    'PAID': 'success',
    'CLOSED': 'info'
  }
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
    } else {
      ElMessage.warning('未找到该快递单号对应的入库包裹，请确认单号是否正确')
    }
  } catch (e) {
    ElMessage.warning('查询包裹信息失败，请确认单号是否正确')
  }
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
  queryForm.orderId = ''
  queryForm.exceptionType = ''
  queryForm.status = ''
  handleQuery()
}

const handleCreate = () => {
  Object.assign(createForm, {
    packageId: '',
    trackingNumber: '',
    orderId: '',
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
  if (!createForm.packageId) {
    ElMessage.warning('未找到对应入库包裹，无法登记异常')
    return
  }
  if (!createForm.orderId) {
    ElMessage.warning('订单号不能为空，异常登记必须绑定订单')
    return
  }
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
    const msg = e?.response?.data?.message || e.message || '未知错误'
    ElMessage.error('登记失败: ' + msg)
  } finally {
    submitLoading.value = false
  }
}

const handleProcess = (row) => {
  Object.assign(processForm, {
    id: row.id,
    trackingNumber: row.trackingNumber,
    orderId: row.orderId,
    packageId: row.packageId,
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
    orderId: row.orderId,
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
    ElMessage.success('补偿已提交，等待审批')
    compensateDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('补偿失败: ' + (e.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

const handleApprove = (row) => {
  Object.assign(approveForm, {
    exceptionId: row.id,
    trackingNumber: row.trackingNumber,
    orderId: row.orderId,
    compensationAmount: row.compensationAmount,
    compensationMethod: row.compensationMethod,
    approver: '',
    approved: true,
    approvalRemark: ''
  })
  approveDialogVisible.value = true
}

const submitApprove = async (isApproved) => {
  if (!approveFormRef.value) return
  approveForm.approved = isApproved
  await approveFormRef.value.validate()
  submitLoading.value = true
  try {
    await approve({
      exceptionId: approveForm.exceptionId,
      approved: approveForm.approved,
      approver: approveForm.approver,
      approvalRemark: approveForm.approvalRemark
    })
    ElMessage.success(isApproved ? '审批通过' : '已驳回')
    approveDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('审批失败: ' + (e.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

const handlePayment = (row) => {
  Object.assign(paymentForm, {
    exceptionId: row.id,
    trackingNumber: row.trackingNumber,
    orderId: row.orderId,
    compensationAmount: row.compensationAmount,
    compensationMethod: row.compensationMethod,
    approver: row.approver,
    approvalTime: row.approvalTime,
    paymentOperator: '',
    paymentRemark: ''
  })
  paymentDialogVisible.value = true
}

const submitPayment = async () => {
  if (!paymentFormRef.value) return
  await paymentFormRef.value.validate()
  submitLoading.value = true
  try {
    await payment({
      exceptionId: paymentForm.exceptionId,
      paymentOperator: paymentForm.paymentOperator,
      paymentRemark: paymentForm.paymentRemark
    })
    ElMessage.success('打款成功')
    paymentDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('打款失败: ' + (e.message || '未知错误'))
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

const handleReprocess = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该异常重新转为处理中？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateExceptionStatus(row.id, { status: 'PROCESSING' })
    ElMessage.success('已重新转为处理中')
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

.bind-success {
  color: #67c23a;
}

.bind-fail {
  color: #f56c6c;
  align-items: center;
  gap: 6px;
}

.standard-hint {
  color: #e6a23c;
  font-weight: 500;
  font-size: 13px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
