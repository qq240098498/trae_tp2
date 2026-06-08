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
        <el-col :span="6">
          <el-statistic title="待补偿" :value="stats.pending">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="已补偿" :value="stats.completed">
            <template #suffix>件</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="补偿总金额" :value="stats.totalAmount" :precision="2">
            <template #prefix>¥</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
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

      <el-table :data="filteredData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="trackingNumber" label="快递单号" width="160" />
        <el-table-column prop="exceptionType" label="异常类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.exceptionType)" size="small">
              {{ getTypeText(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="异常描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="compensationAmount" label="补偿金额" width="120">
          <template #default="{ row }">
            <span v-if="row.compensationAmount != null" class="amount">¥{{ row.compensationAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="compensationMethod" label="补偿方式" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.compensationMethod" type="success" size="small">
              {{ row.compensationMethod }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="compensationTime" label="补偿时间" width="170" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handleRemark" label="处理备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'RESOLVED'"
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCompensationList, updateExceptionStatus } from '@/api/exception'

const loading = ref(false)
const tableData = ref([])

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
  pending: 0,
  completed: 0,
  totalAmount: 0,
  closed: 0
})

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
  const map = { 'PENDING': '待处理', 'PROCESSING': '处理中', 'RESOLVED': '已解决', 'CLOSED': '已关闭' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { 'PENDING': 'danger', 'PROCESSING': 'warning', 'RESOLVED': 'success', 'CLOSED': 'info' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompensationList()
    if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length

      stats.completed = res.filter(item => item.status === 'RESOLVED').length
      stats.closed = res.filter(item => item.status === 'CLOSED').length
      stats.totalAmount = res
        .filter(item => item.compensationAmount != null)
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
