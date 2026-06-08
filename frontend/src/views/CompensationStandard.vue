<template>
  <div class="compensation-standard">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Coin /></el-icon>
            <span>补偿金额标准</span>
          </div>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增标准
          </el-button>
        </div>
      </template>

      <el-alert
        title="补偿金额标准说明"
        description="为每种异常类型设定默认补偿金额，补偿时可自动填充标准金额。每种异常类型仅可设置一条有效标准。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="exceptionType" label="异常类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.exceptionType)" size="small">
              {{ getTypeText(row.exceptionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="standardAmount" label="标准补偿金额" width="160">
          <template #default="{ row }">
            <span class="amount">¥{{ row.standardAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              @change="(val) => handleToggleEnabled(row, val)"
              active-text="启用"
              inactive-text="禁用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑补偿标准' : '新增补偿标准'" width="500px" destroy-on-close>
      <el-form :model="form" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item label="异常类型" prop="exceptionType">
          <el-select v-model="form.exceptionType" placeholder="请选择异常类型" style="width: 100%" :disabled="isEdit">
            <el-option label="破损" value="DAMAGED" />
            <el-option label="丢件" value="LOST" />
            <el-option label="错件" value="WRONG" />
          </el-select>
        </el-form-item>
        <el-form-item label="标准补偿金额" prop="standardAmount">
          <el-input-number v-model="form.standardAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入标准描述" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCompensationStandards,
  createCompensationStandard,
  updateCompensationStandard,
  deleteCompensationStandard
} from '@/api/exception'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: '',
  exceptionType: '',
  standardAmount: 0,
  description: '',
  enabled: true
})

const formRules = {
  exceptionType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  standardAmount: [{ required: true, message: '请输入标准补偿金额', trigger: 'blur' }]
}

const getTypeText = (type) => {
  const map = { 'DAMAGED': '破损', 'LOST': '丢件', 'WRONG': '错件' }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = { 'DAMAGED': 'warning', 'LOST': 'danger', 'WRONG': 'info' }
  return map[type] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCompensationStandards()
    if (Array.isArray(res)) {
      tableData.value = res
    } else {
      tableData.value = []
    }
  } catch (e) {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: '',
    exceptionType: '',
    standardAmount: 0,
    description: '',
    enabled: true
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    exceptionType: row.exceptionType,
    standardAmount: row.standardAmount,
    description: row.description || '',
    enabled: row.enabled
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const data = {
      exceptionType: form.exceptionType,
      standardAmount: form.standardAmount,
      description: form.description,
      enabled: form.enabled
    }
    if (isEdit.value) {
      await updateCompensationStandard(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await createCompensationStandard(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败: ' + (e.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

const handleToggleEnabled = async (row, val) => {
  try {
    await updateCompensationStandard(row.id, { enabled: val })
    row.enabled = val
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除「${getTypeText(row.exceptionType)}」类型的补偿标准？`,
      '提示',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteCompensationStandard(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.compensation-standard {
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

.amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
