<template>
  <div class="price-template">
    <el-card shadow="hover" class="mb-20">
      <template #header>
        <div class="card-header">
          <el-icon><Tickets /></el-icon>
          <span>价格模板维护</span>
          <el-button type="primary" size="small" @click="handleAddTemplate" style="margin-left: auto">
            <el-icon><Plus /></el-icon>
            新增模板
          </el-button>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="快递公司">
          <el-select v-model="filterCompanyId" placeholder="请选择" style="width: 200px" @change="loadTemplates">
            <el-option label="全部" :value="null" />
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="templateData" border stripe v-loading="templateLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="模板名称" width="180" />
        <el-table-column label="所属公司" width="150">
          <template #default="{ row }">
            {{ row.company?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="是否默认" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'success' : 'info'" size="small">
              {{ row.isDefault ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleManageRules(row)">
              价格规则
            </el-button>
            <el-button type="primary" size="small" link @click="handleEditTemplate(row)">
              编辑
            </el-button>
            <el-button
              v-if="!row.isDefault"
              type="success"
              size="small"
              link
              @click="handleSetDefault(row)"
            >
              设为默认
            </el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleTemplate(row)"
            >
              {{ row.enabled ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="hover" v-if="currentTemplate">
      <template #header>
        <div class="card-header">
          <el-icon><Money /></el-icon>
          <span>价格规则 - {{ currentTemplate.name }}</span>
          <el-button type="primary" size="small" @click="handleAddRule" style="margin-left: auto">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </div>
      </template>

      <el-table :data="ruleData" border stripe v-loading="ruleLoading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="重量区间(kg)" width="160">
          <template #default="{ row }">
            {{ row.minWeight }} - {{ row.maxWeight === -1 ? '不限' : row.maxWeight }}
          </template>
        </el-table-column>
        <el-table-column label="基础运费(元)" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 500">¥{{ row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="续重费用(元)" width="120">
          <template #default="{ row }">
            {{ row.additionalPrice ? '¥' + row.additionalPrice : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="续重步长(kg)" width="120">
          <template #default="{ row }">
            {{ row.additionalWeightStep || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEditRule(row)">
              编辑
            </el-button>
            <el-button
              :type="row.enabled ? 'warning' : 'success'"
              size="small"
              link
              @click="handleToggleRule(row)"
            >
              {{ row.enabled ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" link @click="handleDeleteRule(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="templateDialogVisible"
      :title="isEditTemplate ? '编辑价格模板' : '新增价格模板'"
      width="500px"
      @close="resetTemplateForm"
    >
      <el-form :model="templateForm" :rules="templateRules" ref="templateFormRef" label-width="100px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="所属公司" prop="companyId">
          <el-select v-model="templateForm.companyId" placeholder="请选择快递公司" style="width: 100%">
            <el-option v-for="c in companies" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="templateForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch v-model="templateForm.isDefault" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled" v-if="isEditTemplate">
          <el-switch v-model="templateForm.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTemplate" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="ruleDialogVisible"
      :title="isEditRule ? '编辑价格规则' : '新增价格规则'"
      width="500px"
      @close="resetRuleForm"
    >
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="110px">
        <el-form-item label="最小重量(kg)" prop="minWeight">
          <el-input-number v-model="ruleForm.minWeight" :min="0" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大重量(kg)" prop="maxWeight">
          <el-input-number v-model="ruleForm.maxWeight" :min="-1" :precision="2" :step="0.5" style="width: 100%" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">输入 -1 表示不限重量</div>
        </el-form-item>
        <el-form-item label="基础运费(元)" prop="basePrice">
          <el-input-number v-model="ruleForm.basePrice" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续重费用(元)" prop="additionalPrice">
          <el-input-number v-model="ruleForm.additionalPrice" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续重步长(kg)" prop="additionalWeightStep">
          <el-input-number v-model="ruleForm.additionalWeightStep" :min="0" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled" v-if="isEditRule">
          <el-switch v-model="ruleForm.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRule" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCompanies,
  getTemplates,
  createTemplate,
  updateTemplate,
  toggleTemplate,
  setDefaultTemplate,
  getRules,
  createRule,
  updateRule,
  toggleRule,
  deleteRule
} from '@/api/pricing'

const templateLoading = ref(false)
const ruleLoading = ref(false)
const submitting = ref(false)

const filterCompanyId = ref(null)
const companies = ref([])
const templateData = ref([])
const ruleData = ref([])
const currentTemplate = ref(null)

const templateDialogVisible = ref(false)
const ruleDialogVisible = ref(false)
const isEditTemplate = ref(false)
const isEditRule = ref(false)
const templateFormRef = ref(null)
const ruleFormRef = ref(null)

const templateForm = reactive({
  id: null,
  name: '',
  companyId: null,
  description: '',
  isDefault: false,
  enabled: true
})

const ruleForm = reactive({
  id: null,
  templateId: null,
  minWeight: 0,
  maxWeight: 1,
  basePrice: 0,
  additionalPrice: null,
  additionalWeightStep: 1,
  enabled: true
})

const templateRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  companyId: [{ required: true, message: '请选择快递公司', trigger: 'change' }]
}

const ruleRules = {
  minWeight: [{ required: true, message: '请输入最小重量', trigger: 'blur' }],
  maxWeight: [{ required: true, message: '请输入最大重量', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入基础运费', trigger: 'blur' }]
}

const loadCompanies = async () => {
  try {
    const res = await getCompanies({ enabled: true })
    if (Array.isArray(res)) {
      companies.value = res
    }
  } catch (e) {}
}

const loadTemplates = async () => {
  templateLoading.value = true
  try {
    const res = await getTemplates({ companyId: filterCompanyId.value })
    if (Array.isArray(res)) {
      templateData.value = res
    }
  } catch (e) {
    ElMessage.error('加载模板失败')
  } finally {
    templateLoading.value = false
  }
}

const loadRules = async (templateId) => {
  ruleLoading.value = true
  try {
    const res = await getRules({ templateId })
    if (Array.isArray(res)) {
      ruleData.value = res
    }
  } catch (e) {
    ElMessage.error('加载规则失败')
  } finally {
    ruleLoading.value = false
  }
}

const handleManageRules = (row) => {
  currentTemplate.value = row
  loadRules(row.id)
}

const handleAddTemplate = () => {
  isEditTemplate.value = false
  templateDialogVisible.value = true
}

const handleEditTemplate = (row) => {
  isEditTemplate.value = true
  templateForm.id = row.id
  templateForm.name = row.name
  templateForm.companyId = row.company?.id
  templateForm.description = row.description
  templateForm.isDefault = row.isDefault
  templateForm.enabled = row.enabled
  templateDialogVisible.value = true
}

const handleSetDefault = async (row) => {
  try {
    await ElMessageBox.confirm('确认将该模板设为默认模板？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await setDefaultTemplate(row.id)
    ElMessage.success('设置成功')
    loadTemplates()
  } catch (e) {}
}

const handleToggleTemplate = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认${row.enabled ? '停用' : '启用'}该模板？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await toggleTemplate(row.id, !row.enabled)
    row.enabled = !row.enabled
    ElMessage.success('操作成功')
  } catch (e) {}
}

const resetTemplateForm = () => {
  templateForm.id = null
  templateForm.name = ''
  templateForm.companyId = null
  templateForm.description = ''
  templateForm.isDefault = false
  templateForm.enabled = true
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
}

const handleSubmitTemplate = async () => {
  if (!templateFormRef.value) return
  await templateFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEditTemplate.value) {
          await updateTemplate(templateForm.id, templateForm)
          ElMessage.success('修改成功')
        } else {
          await createTemplate(templateForm)
          ElMessage.success('新增成功')
        }
        templateDialogVisible.value = false
        loadTemplates()
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleAddRule = () => {
  if (!currentTemplate.value) {
    ElMessage.warning('请先选择一个模板')
    return
  }
  isEditRule.value = false
  ruleForm.templateId = currentTemplate.value.id
  ruleDialogVisible.value = true
}

const handleEditRule = (row) => {
  isEditRule.value = true
  ruleForm.id = row.id
  ruleForm.templateId = row.template?.id || currentTemplate.value.id
  ruleForm.minWeight = row.minWeight
  ruleForm.maxWeight = row.maxWeight
  ruleForm.basePrice = row.basePrice
  ruleForm.additionalPrice = row.additionalPrice
  ruleForm.additionalWeightStep = row.additionalWeightStep
  ruleForm.enabled = row.enabled
  ruleDialogVisible.value = true
}

const handleToggleRule = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认${row.enabled ? '停用' : '启用'}该规则？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await toggleRule(row.id, !row.enabled)
    row.enabled = !row.enabled
    ElMessage.success('操作成功')
  } catch (e) {}
}

const handleDeleteRule = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该价格规则？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRule(row.id)
    ElMessage.success('删除成功')
    loadRules(currentTemplate.value.id)
  } catch (e) {}
}

const resetRuleForm = () => {
  ruleForm.id = null
  ruleForm.templateId = currentTemplate.value?.id
  ruleForm.minWeight = 0
  ruleForm.maxWeight = 1
  ruleForm.basePrice = 0
  ruleForm.additionalPrice = null
  ruleForm.additionalWeightStep = 1
  ruleForm.enabled = true
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
}

const handleSubmitRule = async () => {
  if (!ruleFormRef.value) return
  await ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEditRule.value) {
          await updateRule(ruleForm.id, ruleForm)
          ElMessage.success('修改成功')
        } else {
          await createRule(ruleForm)
          ElMessage.success('新增成功')
        }
        ruleDialogVisible.value = false
        loadRules(currentTemplate.value.id)
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadCompanies()
  loadTemplates()
})
</script>

<style scoped>
.price-template {
  padding: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.filter-form {
  margin-bottom: 15px;
}
</style>
