<template>
  <div class="courier-reconciliation">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><DocumentChecked /></el-icon>
          <span>快递员对账</span>
        </div>
      </template>

      <div class="filter-bar">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 300px"
        />
        <el-select v-model="filterCourier" placeholder="快递公司" clearable style="width: 150px">
          <el-option
            v-for="c in courierOptions"
            :key="c"
            :label="c"
            :value="c"
          />
        </el-select>
        <el-select v-model="filterStatus" placeholder="对账状态" clearable style="width: 130px">
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
        </el-select>
        <el-button type="primary" @click="loadData">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button type="success" @click="handleGenerate" style="margin-left: auto">
          <el-icon><Refresh /></el-icon>
          生成对账
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" show-summary :summary-method="getSummary">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="reconciliationDate" label="对账日期" width="120" />
        <el-table-column prop="courier" label="快递公司" width="130" />
        <el-table-column prop="totalIn" label="入库数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.totalIn }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalPickedUp" label="已取件数量" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.totalPickedUp }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalOverdue" label="滞留数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totalOverdue > 0 ? 'danger' : 'info'" size="small">{{ row.totalOverdue }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="未取件数量" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">{{ row.totalIn - row.totalPickedUp - row.totalOverdue }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="取件率" width="100" align="center">
          <template #default="{ row }">
            <span :class="getPickupRateClass(row)">{{ getPickupRate(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CONFIRMED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'CONFIRMED' ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              link
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              type="primary"
              size="small"
              link
              @click="handleRemark(row)"
            >
              备注
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="generateDialogVisible"
      title="生成对账记录"
      width="400px"
    >
      <el-form label-width="80px">
        <el-form-item label="对账日期">
          <el-date-picker
            v-model="generateDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGenerate" :loading="generating">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="remarkDialogVisible"
      title="编辑备注"
      width="400px"
    >
      <el-input
        v-model="remarkContent"
        type="textarea"
        :rows="4"
        placeholder="请输入备注内容"
      />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemark" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReconciliations,
  generateReconciliation,
  confirmReconciliation,
  updateReconciliationRemark,
  getCourierList
} from '@/api/reconciliation'

const loading = ref(false)
const generating = ref(false)
const submitting = ref(false)
const tableData = ref([])

const dateRange = ref(null)
const filterCourier = ref('')
const filterStatus = ref('')
const courierOptions = ref([])

const generateDialogVisible = ref(false)
const generateDate = ref('')

const remarkDialogVisible = ref(false)
const remarkContent = ref('')
const currentRemarkId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (filterCourier.value) params.courier = filterCourier.value
    if (filterStatus.value) params.status = filterStatus.value

    const res = await getReconciliations(params)
    if (Array.isArray(res)) {
      tableData.value = res
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadCouriers = async () => {
  try {
    const res = await getCourierList()
    if (Array.isArray(res)) {
      courierOptions.value = res
    }
  } catch (e) {}
}

const handleGenerate = () => {
  const today = new Date()
  const y = today.getFullYear()
  const m = String(today.getMonth() + 1).padStart(2, '0')
  const d = String(today.getDate()).padStart(2, '0')
  generateDate.value = `${y}-${m}-${d}`
  generateDialogVisible.value = true
}

const submitGenerate = async () => {
  if (!generateDate.value) {
    ElMessage.warning('请选择对账日期')
    return
  }
  generating.value = true
  try {
    await generateReconciliation({ date: generateDate.value })
    ElMessage.success('对账记录生成成功')
    generateDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认 ${row.reconciliationDate} ${row.courier} 的对账记录？确认后不可修改。`,
      '确认对账',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await confirmReconciliation(row.id)
    ElMessage.success('对账确认成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.response) {
      ElMessage.error(e.response?.data?.message || '确认失败')
    }
  }
}

const handleRemark = (row) => {
  currentRemarkId.value = row.id
  remarkContent.value = row.remark || ''
  remarkDialogVisible.value = true
}

const submitRemark = async () => {
  submitting.value = true
  try {
    await updateReconciliationRemark(currentRemarkId.value, { remark: remarkContent.value })
    ElMessage.success('备注更新成功')
    remarkDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('备注更新失败')
  } finally {
    submitting.value = false
  }
}

const getPickupRate = (row) => {
  if (row.totalIn === 0) return '0%'
  const rate = ((row.totalPickedUp / row.totalIn) * 100).toFixed(1)
  return rate + '%'
}

const getPickupRateClass = (row) => {
  if (row.totalIn === 0) return 'rate-zero'
  const rate = (row.totalPickedUp / row.totalIn) * 100
  if (rate >= 80) return 'rate-high'
  if (rate >= 50) return 'rate-medium'
  return 'rate-low'
}

const getSummary = ({ columns, data }) => {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (index === 1 || index === 2 || index === 8 || index === 10) {
      sums[index] = ''
      return
    }
    const prop = column.property
    if (prop === 'totalIn' || prop === 'totalPickedUp' || prop === 'totalOverdue') {
      const values = data.map(item => Number(item[prop]))
      sums[index] = values.reduce((prev, curr) => prev + curr, 0)
    } else if (index === 6) {
      const totalIn = data.reduce((prev, curr) => prev + Number(curr.totalIn), 0)
      const totalPickedUp = data.reduce((prev, curr) => prev + Number(curr.totalPickedUp), 0)
      const totalOverdue = data.reduce((prev, curr) => prev + Number(curr.totalOverdue), 0)
      sums[index] = totalIn - totalPickedUp - totalOverdue
    } else if (index === 7) {
      const totalIn = data.reduce((prev, curr) => prev + Number(curr.totalIn), 0)
      const totalPickedUp = data.reduce((prev, curr) => prev + Number(curr.totalPickedUp), 0)
      if (totalIn === 0) {
        sums[index] = '0%'
      } else {
        sums[index] = ((totalPickedUp / totalIn) * 100).toFixed(1) + '%'
      }
    } else {
      sums[index] = ''
    }
  })
  return sums
}

onMounted(() => {
  loadCouriers()
  loadData()
})
</script>

<style scoped>
.courier-reconciliation {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.rate-high {
  color: #67c23a;
  font-weight: 600;
}

.rate-medium {
  color: #e6a23c;
  font-weight: 600;
}

.rate-low {
  color: #f56c6c;
  font-weight: 600;
}

.rate-zero {
  color: #909399;
}
</style>
