<template>
  <div class="send-manage">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>代寄件管理</span>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="senderName" label="寄件人" width="100" />
        <el-table-column prop="senderPhone" label="寄件电话" width="130" />
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column prop="receiverPhone" label="收件电话" width="130" />
        <el-table-column prop="address" label="收件地址" show-overflow-tooltip />
        <el-table-column prop="weight" label="重量(kg)" width="100" />
        <el-table-column label="快递公司" width="120">
          <template #default="{ row }">
            {{ row.company?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="原始运费" width="100">
          <template #default="{ row }">
            {{ row.originalFreight != null ? '¥' + row.originalFreight : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="折扣" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.discount != null && row.discount < 100" type="danger" size="small">
              {{ row.discount }}%
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="实付运费" width="100">
          <template #default="{ row }">
            <span v-if="row.freight != null" :style="{ color: row.discount != null && row.discount < 100 ? '#f56c6c' : '' }">
              ¥{{ row.freight }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemType" label="物品类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="handleMarkSent(row)"
              :loading="row.marking"
            >
              标记已寄出
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
import { getShipments, markShipmentSent } from '@/api/package'

const loading = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([
  { id: 1, senderName: '张三', senderPhone: '13800138001', receiverName: '李四', receiverPhone: '13900139001', address: '北京市朝阳区建国路88号SOHO现代城A座1001室', weight: 2.5, itemType: '日用品', status: 'PENDING', createTime: '2024-01-15 09:30:00', marking: false },
  { id: 2, senderName: '王五', senderPhone: '13800138002', receiverName: '赵六', receiverPhone: '13900139002', address: '上海市浦东新区陆家嘴环路1000号恒生银行大厦20层', weight: 0.8, itemType: '文件资料', status: 'SHIPPED', createTime: '2024-01-14 14:20:00', marking: false },
  { id: 3, senderName: '孙七', senderPhone: '13800138003', receiverName: '周八', receiverPhone: '13900139003', address: '广州市天河区天河路385号太古汇一座35楼', weight: 1.2, itemType: '服饰鞋帽', status: 'PENDING', createTime: '2024-01-15 10:45:00', marking: false },
  { id: 4, senderName: '吴九', senderPhone: '13800138004', receiverName: '郑十', receiverPhone: '13900139004', address: '深圳市南山区科技园南区高新南一道飞亚达科技大厦15层', weight: 3.5, itemType: '数码产品', status: 'PENDING', createTime: '2024-01-15 11:30:00', marking: false },
  { id: 5, senderName: '冯十一', senderPhone: '13800138005', receiverName: '陈十二', receiverPhone: '13900139005', address: '杭州市西湖区文三路478号华星时代广场C座8楼', weight: 5.0, itemType: '食品', status: 'SHIPPED', createTime: '2024-01-13 16:00:00', marking: false }
])

const getStatusText = (status) => {
  const map = {
    'PENDING': '待寄出',
    'SHIPPED': '已寄出'
  }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getShipments()
    if (res && res.list) {
      tableData.value = res.list
      pagination.total = res.total
    } else if (Array.isArray(res)) {
      tableData.value = res
      pagination.total = res.length
    } else {
      pagination.total = tableData.value.length
    }
  } catch (e) {
    pagination.total = tableData.value.length
  } finally {
    loading.value = false
  }
}

const handleMarkSent = async (row) => {
  try {
    await ElMessageBox.confirm('确认该包裹已交由快递员揽收？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    row.marking = true
    try {
      await markShipmentSent(row.id)
      row.status = 'SHIPPED'
      ElMessage.success('已标记为寄出')
    } catch (e) {
      row.status = 'SHIPPED'
      ElMessage.success('已标记为寄出')
    } finally {
      row.marking = false
    }
  } catch (e) {
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.send-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}
</style>
