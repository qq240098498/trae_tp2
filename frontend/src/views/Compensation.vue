<template>
  <div class="compensation">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Money /></el-icon>
            <span>补偿管理</span>
          </div>
        </div>
      </template>

      <el-row :gutter="20" class="stat-row">
        <el-col :span="4">
          <el-statistic title="待审批" :value="stats.approvalPending">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="审批通过" :value="stats.approved">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="审批驳回" :value="stats.rejected">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="已打款" :value="stats.paid">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="补偿总金额" :value="stats.totalAmount" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-col>
        <el-col :span="4">
          <el-statistic title="已关闭" :value="stats.closed">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-divider />

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="快递单号">
          <el-input v-model="queryForm.trackingNumber" placeholder="请输入快递单号" clearable />
        </el-form-item>
        <el-form-item label="补偿方式">
          <el-select v-model="queryForm.compensationMethod" placeholder="全部方式" clearable style="width: 140px">
            <el-option label="现金退款" value="现金退款" />
            <el-option label="重新发货" value="重新发货" />
            <el-option label="代金券" value="代金券" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待审批" value="APPROVAL_PENDING" />
            <el-option label="审批通过" value="APPROVED" />
            <el-option label="审批驳回" value="REJECTED" />
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

      <el-table :data="filteredData" border stripe v-loading="loading">
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
        <el-table-column prop="description" label="异常描述" min-width="120" show-overflow-tooltip />
        <el-table-column prop="compensationAmount" label="补偿金额" width="110">
          <template #default="{ row }">
            <span v-if="row.compensationAmount != null" class="amount">¥{{ row.compensationAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="compensationMethod" label="补偿方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.compensationMethod" type="success" size="small">
              {{ row.compensationMethod }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="80">
          <template #default="{ row }">
            {{ row.approver || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentOperator" label="打款人" width="80">
          <template #default="{ row }">
            {{ row.paymentOperator || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="打款时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCompensationList, updateExceptionStatus, approve, payment } from '@/api/exception'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])

const approveDialogVisible = ref(false)
const paymentDialogVisible = ref(false)

const approveFormRef = ref(null)
const paymentFormRef = ref(null)

const queryForm = reactive({
  trackingNumber: '',
  compensationMethod: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const stats = reactive({
  approvalPending: 0,
  approved: 0,
  rejected: 0,
  paid: 0,
  totalAmount: 0,
  closed: 0
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

const approveRules = {
  approver: [{ required: true, message: '请输入审批人', trigger: 'blur' }],
  approved: [{ required: true, message: '请选择审批结果', trigger: 'change' }]
}

const paymentRules = {
  paymentOperator: [{ required: true, message: '请输入打款操作人', trigger: 'blur' }]
}

const filteredData = computed(() => {
  let data = tableData.value
  if (queryForm.trackingNumber) {
    data = data.filter(item => item.trackingNumber && item.trackingNumber.includes(queryForm.trackingNumber))
  }
  if (queryForm.compensationMethod) {
    data = data.filter(item => item.compensationMethod === queryForm.compensationMethod)
  }
  if (queryForm.status) {
    data = data.filter(item => item.status === queryForm.status)
  }
  return data
})

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompensationList()
    if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length

      stats.approvalPending = res.filter(item => item.status === 'APPROVAL_PENDING').length
      stats.approved = res.filter(item => item.status === 'APPROVED').length
      stats.rejected = res.filter(item => item.status === 'REJECTED').length
      stats.paid = res.filter(item => item.status === 'PAID').length
      stats.closed = res.filter(item => item.status === 'CLOSED').length
      stats.totalAmount = res
        .filter(item => item.compensationAmount != null && item.status === 'PAID')
        .reduce((sum, item) => sum + Number(item.compensationAmount), 0)
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
}

const handleReset = () => {
  queryForm.trackingNumber = ''
  queryForm.compensationMethod = ''
  queryForm.status = ''
  handleQuery()
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
    await ElMessageBox.confirm('确认关闭该补偿记录？', '提示', {
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

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.compensation {
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

.stat-row {
  margin-bottom: 10px;
}

.query-form {
  margin-bottom: 20px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
