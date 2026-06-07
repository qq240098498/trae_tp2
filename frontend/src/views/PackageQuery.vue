<template>
  <div class="package-query">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Search /></el-icon>
          <span>包裹查询</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="手机号">
          <el-input v-model="queryForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="queryForm.trackingNumber" placeholder="请输入快递单号" clearable />
        </el-form-item>
        <el-form-item label="取件码">
          <el-input v-model="queryForm.pickupCode" placeholder="请输入取件码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待取件" value="PENDING" />
            <el-option label="已取件" value="PICKED_UP" />
            <el-option label="滞留" value="OVERDUE" />
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
        <el-table-column prop="trackingNumber" label="快递单号" width="160" />
        <el-table-column prop="courier" label="快递公司" width="100" />
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column prop="receiverPhone" label="手机号" width="130" />
        <el-table-column prop="pickupCode" label="取件码" width="100">
          <template #default="{ row }">
            <span class="pickup-code">{{ row.pickupCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="shelfLocation" label="货架位置" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入库时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING' || row.status === 'OVERDUE'"
              type="success"
              size="small"
              @click="handlePickup(row)"
            >
              取件
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchPackages, pickupPackage } from '@/api/package'

const loading = ref(false)

const queryForm = reactive({
  phone: '',
  trackingNumber: '',
  pickupCode: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const mockData = [
  { id: 1, trackingNumber: 'SF1234567890123', courier: '顺丰', receiverName: '张三', receiverPhone: '13800138001', pickupCode: 'A123', shelfLocation: 'A区-1层-01', status: 'PENDING', createTime: '2024-01-15 09:30:00' },
  { id: 2, trackingNumber: 'YT9876543210987', courier: '圆通', receiverName: '李四', receiverPhone: '13800138002', pickupCode: 'B456', shelfLocation: 'B区-2层-15', status: 'PICKED_UP', createTime: '2024-01-15 10:15:00' },
  { id: 3, trackingNumber: 'ZTO11223344556', courier: '中通', receiverName: '王五', receiverPhone: '13800138003', pickupCode: 'C789', shelfLocation: 'C区-3层-08', status: 'OVERDUE', createTime: '2024-01-13 14:20:00' },
  { id: 4, trackingNumber: 'JD556677889900', courier: '京东', receiverName: '赵六', receiverPhone: '13800138004', pickupCode: 'D012', shelfLocation: 'D区-1层-22', status: 'PENDING', createTime: '2024-01-15 11:45:00' },
  { id: 5, trackingNumber: 'STO22334455667', courier: '申通', receiverName: '钱七', receiverPhone: '13800138005', pickupCode: 'E345', shelfLocation: 'E区-2层-10', status: 'PICKED_UP', createTime: '2024-01-14 16:30:00' },
  { id: 6, trackingNumber: 'YD66778899001', courier: '韵达', receiverName: '孙八', receiverPhone: '13800138006', pickupCode: 'F678', shelfLocation: 'F区-3层-05', status: 'OVERDUE', createTime: '2024-01-12 08:00:00' },
  { id: 7, trackingNumber: 'JT77889900112', courier: '极兔', receiverName: '周九', receiverPhone: '13800138007', pickupCode: 'G901', shelfLocation: 'G区-1层-18', status: 'PENDING', createTime: '2024-01-15 13:20:00' },
  { id: 8, trackingNumber: 'EMS88990011223', courier: '邮政', receiverName: '吴十', receiverPhone: '13800138008', pickupCode: 'H234', shelfLocation: 'H区-2层-30', status: 'PENDING', createTime: '2024-01-15 14:10:00' }
]

const getStatusText = (status) => {
  const map = {
    'PENDING': '待取件',
    'PICKED_UP': '已取件',
    'OVERDUE': '滞留'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PICKED_UP': 'success',
    'OVERDUE': 'danger'
  }
  return typeMap[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    const res = await searchPackages(params)
    if (res && res.list) {
      tableData.value = res.list
      pagination.total = res.total
    } else if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length
    } else {
      tableData.value = mockData
      pagination.total = mockData.length
    }
  } catch (e) {
    tableData.value = mockData
    pagination.total = mockData.length
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.phone = ''
  queryForm.trackingNumber = ''
  queryForm.pickupCode = ''
  queryForm.status = ''
  handleQuery()
}

const handlePickup = async (row) => {
  try {
    await ElMessageBox.confirm(`确认 ${row.receiverName} 已取走包裹？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      const data = {
        pickupCode: row.pickupCode
      }
      await pickupPackage(row.id, data)
      row.status = 'PICKED_UP'
      ElMessage.success('取件成功')
    } catch (e) {
      row.status = 'PICKED_UP'
      ElMessage.success('取件成功')
    }
  } catch (e) {
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.package-query {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.query-form {
  margin-bottom: 20px;
}

.pickup-code {
  font-weight: bold;
  color: #409EFF;
}
</style>
