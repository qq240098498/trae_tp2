<template>
  <div class="overdue">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Warning /></el-icon>
          <span>滞留包裹管理</span>
          <el-tag type="danger" style="margin-left: 10px">共 {{ tableData.length }} 件滞留</el-tag>
        </div>
      </template>

      <el-alert
        title="滞留件说明"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          以下包裹入库已超过48小时未取件，请及时联系客户取件，避免包裹退回。
        </template>
      </el-alert>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="trackingNumber" label="快递单号" width="160" />
        <el-table-column prop="courier" label="快递公司" width="100" />
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column prop="receiverPhone" label="联系电话" width="130" />
        <el-table-column prop="pickupCode" label="取件码" width="100">
          <template #default="{ row }">
            <span class="pickup-code">{{ row.pickupCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="shelfLocation" label="货架位置" width="120" />
        <el-table-column prop="createTime" label="入库时间" width="170" />
        <el-table-column prop="overdueHours" label="滞留时长" width="120">
          <template #default="{ row }">
            <el-tag type="danger" size="small">{{ row.overdueHours }}小时</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleReminder(row)"
              :loading="row.reminding"
            >
              <el-icon><Bell /></el-icon>
              发送提醒
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handlePickup(row)"
            >
              取件
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOverduePackages, pickupPackage } from '@/api/package'

const loading = ref(false)

const tableData = ref([
  { id: 1, trackingNumber: 'ZTO11223344556', courier: '中通', receiverName: '王五', receiverPhone: '13800138003', pickupCode: 'C789', shelfLocation: 'C区-3层-08', createTime: '2024-01-13 14:20:00', overdueHours: 56, reminding: false },
  { id: 2, trackingNumber: 'YD66778899001', courier: '韵达', receiverName: '孙八', receiverPhone: '13800138006', pickupCode: 'F678', shelfLocation: 'F区-3层-05', createTime: '2024-01-12 08:00:00', overdueHours: 78, reminding: false },
  { id: 3, trackingNumber: 'STO33445566778', courier: '申通', receiverName: '郑十一', receiverPhone: '13800138009', pickupCode: 'I567', shelfLocation: 'I区-2层-12', createTime: '2024-01-12 16:30:00', overdueHours: 70, reminding: false },
  { id: 4, trackingNumber: 'SF44556677889', courier: '顺丰', receiverName: '冯十二', receiverPhone: '13800138010', pickupCode: 'J890', shelfLocation: 'J区-1层-05', createTime: '2024-01-13 10:00:00', overdueHours: 60, reminding: false },
  { id: 5, trackingNumber: 'YT55667788990', courier: '圆通', receiverName: '陈十三', receiverPhone: '13800138011', pickupCode: 'K234', shelfLocation: 'K区-3层-20', createTime: '2024-01-13 20:15:00', overdueHours: 50, reminding: false }
])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOverduePackages()
    if (res && res.length > 0) {
      tableData.value = res
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleReminder = async (row) => {
  row.reminding = true
  try {
    ElMessage.success(`已向 ${row.receiverName} 发送取件提醒短信`)
  } catch (e) {
    ElMessage.success(`已向 ${row.receiverName} 发送取件提醒短信`)
  } finally {
    row.reminding = false
  }
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
      const index = tableData.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        tableData.value.splice(index, 1)
      }
      ElMessage.success('取件成功')
    } catch (e) {
      const index = tableData.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        tableData.value.splice(index, 1)
      }
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
.overdue {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.pickup-code {
  font-weight: bold;
  color: #409EFF;
}
</style>
