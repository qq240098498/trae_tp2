<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card card-1" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="40"><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">今日入库</div>
              <div class="stat-value">{{ stats.todayIn }}</div>
              <div class="stat-desc">较昨日 <span class="increase">+12%</span></div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card card-2" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="40"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">待取件数</div>
              <div class="stat-value">{{ stats.pendingPickup }}</div>
              <div class="stat-desc">需尽快通知客户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card card-3" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="40"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">滞留件数</div>
              <div class="stat-value">{{ stats.overdue }}</div>
              <div class="stat-desc">超过48小时未取</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card card-4" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="40"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">待寄出代寄</div>
              <div class="stat-value">{{ stats.pendingSend }}</div>
              <div class="stat-desc">等待快递员揽收</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="$router.push('/package-in')">
              <el-icon><Download /></el-icon>
              包裹入库
            </el-button>
            <el-button type="success" size="large" @click="$router.push('/package-pickup')">
              <el-icon><Goods /></el-icon>
              客户取件
            </el-button>
            <el-button type="warning" size="large" @click="$router.push('/寄件登记')">
              <el-icon><Edit /></el-icon>
              代寄登记
            </el-button>
            <el-button type="info" size="large" @click="$router.push('/package-query')">
              <el-icon><Search /></el-icon>
              包裹查询
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近入库</span>
            </div>
          </template>
          <el-table :data="recentPackages" size="small">
            <el-table-column prop="trackingNo" label="快递单号" width="150" />
            <el-table-column prop="receiverName" label="收件人" width="100" />
            <el-table-column prop="company" label="快递公司" width="100" />
            <el-table-column prop="pickupCode" label="取件码" width="100" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === '待取件' ? 'warning' : 'success'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboardStats } from '@/api/package'

const stats = ref({
  todayIn: 0,
  pendingPickup: 0,
  overdue: 0,
  pendingSend: 0
})

const recentPackages = ref([
  { trackingNo: 'SF1234567890', receiverName: '张三', company: '顺丰', pickupCode: 'A123', status: '待取件' },
  { trackingNo: 'YT9876543210', receiverName: '李四', company: '圆通', pickupCode: 'B456', status: '待取件' },
  { trackingNo: 'ZTO11223344', receiverName: '王五', company: '中通', pickupCode: 'C789', status: '已取件' },
  { trackingNo: 'JD55667788', receiverName: '赵六', company: '京东', pickupCode: 'D012', status: '待取件' }
])

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res) {
      stats.value = res
    }
  } catch (e) {
    stats.value = {
      todayIn: 56,
      pendingPickup: 32,
      overdue: 8,
      pendingSend: 12
    }
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  border: none;
  border-radius: 8px;
  overflow: hidden;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.card-1 .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-2 .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-3 .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-4 .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 6px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

.increase {
  color: #67c23a;
  font-weight: 500;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.quick-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 10px 0;
}

.quick-actions .el-button {
  height: 60px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
</style>
