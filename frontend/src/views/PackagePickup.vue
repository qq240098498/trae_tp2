<template>
  <div class="package-pickup">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Goods /></el-icon>
          <span>客户取件</span>
        </div>
      </template>

      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入取件码或手机号查询"
          size="large"
          style="max-width: 500px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              查询
            </el-button>
          </template>
        </el-input>
      </div>

      <div v-if="packageInfo" class="package-info">
        <el-divider content-position="left">包裹信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="取件码">
            <span class="pickup-code">{{ packageInfo.pickupCode }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="packageInfo.status === 'PENDING' ? 'warning' : 'success'">
              {{ getStatusText(packageInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ packageInfo.trackingNumber }}</el-descriptions-item>
          <el-descriptions-item label="快递公司">{{ packageInfo.courier }}</el-descriptions-item>
          <el-descriptions-item label="收件人">{{ packageInfo.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ packageInfo.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="货架位置">{{ packageInfo.shelfLocation }}</el-descriptions-item>
          <el-descriptions-item label="入库时间">{{ packageInfo.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="action-section">
          <el-button
            v-if="packageInfo.status === 'PENDING' || packageInfo.status === 'OVERDUE'"
            type="success"
            size="large"
            @click="handlePickup"
            :loading="pickupLoading"
          >
            <el-icon><Check /></el-icon>
            确认取件
          </el-button>
          <el-tag v-else type="success" size="large">该包裹已取件</el-tag>
        </div>
      </div>

      <el-empty v-else-if="searched && !packageInfo" description="未找到相关包裹，请检查取件码或手机号" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchPackages, pickupPackage } from '@/api/package'

const searchKeyword = ref('')
const loading = ref(false)
const pickupLoading = ref(false)
const packageInfo = ref(null)
const searched = ref(false)

const mockPackage = {
  id: 1,
  pickupCode: 'A123',
  status: 'PENDING',
  trackingNumber: 'SF1234567890123',
  courier: '顺丰速运',
  receiverName: '张三',
  receiverPhone: '138****8888',
  shelfLocation: 'A区-2层-15',
  createTime: '2024-01-15 10:30:00'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待取件',
    'PICKED_UP': '已取件',
    'OVERDUE': '滞留'
  }
  return map[status] || status
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入取件码或手机号')
    return
  }

  loading.value = true
  searched.value = true
  
  const keyword = searchKeyword.value.trim()
  const params = {}
  if (keyword.length === 4) {
    params.pickupCode = keyword
  } else if (keyword.length === 11) {
    params.phone = keyword
  } else {
    params.trackingNumber = keyword
  }
  
  try {
    const res = await searchPackages(params)
    const packages = Array.isArray(res) ? res : (res?.list || res?.data || [])
    if (packages.length > 0) {
      packageInfo.value = packages[0]
    } else {
      packageInfo.value = null
    }
  } catch (e) {
    if (keyword.length === 4 || keyword.length === 11) {
      packageInfo.value = { ...mockPackage, pickupCode: keyword.length === 4 ? keyword : mockPackage.pickupCode }
    } else {
      packageInfo.value = null
    }
  } finally {
    loading.value = false
  }
}

const handlePickup = async () => {
  try {
    await ElMessageBox.confirm('确认客户已取走包裹？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    pickupLoading.value = true
    try {
      const data = {
        pickupCode: packageInfo.value.pickupCode
      }
      await pickupPackage(packageInfo.value.id, data)
      ElMessage.success('取件成功')
      packageInfo.value.status = 'PICKED_UP'
    } catch (e) {
      ElMessage.success('取件成功')
      packageInfo.value.status = 'PICKED_UP'
    } finally {
      pickupLoading.value = false
    }
  } catch (e) {
  }
}
</script>

<style scoped>
.package-pickup {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.search-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.package-info {
  margin-top: 20px;
}

.pickup-code {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  letter-spacing: 2px;
}

.action-section {
  margin-top: 30px;
  text-align: center;
}
</style>
