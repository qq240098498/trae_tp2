<template>
  <div class="send-register">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Edit /></el-icon>
          <span>代寄件登记</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        style="max-width: 700px; margin: 0 auto;"
      >
        <el-divider content-position="left">会员信息</el-divider>

        <el-form-item label="选择会员">
          <el-select
            v-model="form.memberId"
            placeholder="请选择会员（可选）"
            clearable
            filterable
            remote
            :remote-method="searchMembers"
            :loading="memberLoading"
            size="large"
            style="width: 100%"
            @change="handleMemberChange"
          >
            <el-option
              v-for="m in memberOptions"
              :key="m.id"
              :label="`${m.name} (${m.phone})`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="会员折扣" v-if="selectedMember">
          <el-tag type="danger" size="large">
            {{ selectedMember.discount }}%（{{ discountText }}）
          </el-tag>
        </el-form-item>

        <el-divider content-position="left">寄件人信息</el-divider>

        <el-form-item label="寄件人姓名" prop="senderName">
          <el-input
            v-model="form.senderName"
            placeholder="请输入寄件人姓名"
            size="large"
          />
        </el-form-item>

        <el-form-item label="寄件人电话" prop="senderPhone">
          <el-input
            v-model="form.senderPhone"
            placeholder="请输入寄件人手机号"
            size="large"
          />
        </el-form-item>

        <el-divider content-position="left">收件人信息</el-divider>

        <el-form-item label="收件人姓名" prop="receiverName">
          <el-input
            v-model="form.receiverName"
            placeholder="请输入收件人姓名"
            size="large"
          />
        </el-form-item>

        <el-form-item label="收件人电话" prop="receiverPhone">
          <el-input
            v-model="form.receiverPhone"
            placeholder="请输入收件人手机号"
            size="large"
          />
        </el-form-item>

        <el-form-item label="收件地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="3"
            placeholder="请输入详细收件地址"
            size="large"
          />
        </el-form-item>

        <el-divider content-position="left">包裹与运费信息</el-divider>

        <el-form-item label="快递公司" prop="companyId">
          <el-select
            v-model="form.companyId"
            placeholder="请选择快递公司"
            size="large"
            style="width: 100%"
            filterable
            @change="handleCompanyChange"
          >
            <el-option
              v-for="c in companies"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="价格模板" v-if="form.companyId">
          <el-select
            v-model="form.templateId"
            placeholder="请选择价格模板"
            size="large"
            style="width: 100%"
            clearable
            @change="calculatePrice"
          >
            <el-option
              v-for="t in templates"
              :key="t.id"
              :label="t.name + (t.isDefault ? '（默认）' : '')"
              :value="t.id"
            />
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 4px">
            不选则使用该快递公司的默认模板
          </div>
        </el-form-item>

        <el-form-item label="物品重量(kg)" prop="weight">
          <el-input-number
            v-model="form.weight"
            :min="0.1"
            :max="50"
            :step="0.1"
            size="large"
            style="width: 100%"
            @change="calculatePrice"
          />
        </el-form-item>

        <el-form-item label="物品类型">
          <el-select
            v-model="form.itemType"
            placeholder="请选择物品类型"
            size="large"
            style="width: 100%"
          >
            <el-option label="日用品" value="日用品" />
            <el-option label="服饰鞋帽" value="服饰鞋帽" />
            <el-option label="数码产品" value="数码产品" />
            <el-option label="食品" value="食品" />
            <el-option label="文件资料" value="文件资料" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="运费预览" v-if="priceInfo.originalFreight !== null">
          <div class="price-preview">
            <div class="price-row">
              <span>原始运费：</span>
              <span class="price-value">¥{{ priceInfo.originalFreight }}</span>
            </div>
            <div class="price-row" v-if="selectedMember && selectedMember.discount < 100">
              <span>会员折扣：</span>
              <el-tag type="danger" size="small">{{ selectedMember.discount }}%（{{ discountText }}）</el-tag>
            </div>
            <div class="price-row" v-if="selectedMember && selectedMember.discount < 100">
              <span>优惠金额：</span>
              <span class="price-save">-¥{{ priceInfo.discountAmount }}</span>
            </div>
            <el-divider style="margin: 8px 0" />
            <div class="price-row">
              <span class="price-final-label">实付运费：</span>
              <span class="price-final">¥{{ priceInfo.finalFreight }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息（可选）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" @click="handleSubmit" :loading="loading">
            <el-icon><Check /></el-icon>
            提交登记
          </el-button>
          <el-button size="large" @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createShipment } from '@/api/package'
import { getMembers } from '@/api/member'
import { getCompanies, getTemplates, calculatePrice as apiCalculatePrice } from '@/api/pricing'

const formRef = ref(null)
const loading = ref(false)
const memberLoading = ref(false)
const memberOptions = ref([])
const selectedMember = ref(null)
const companies = ref([])
const templates = ref([])

const priceInfo = reactive({
  originalFreight: null,
  discountAmount: null,
  finalFreight: null
})

const form = reactive({
  memberId: null,
  senderName: '',
  senderPhone: '',
  receiverName: '',
  receiverPhone: '',
  address: '',
  companyId: null,
  templateId: null,
  weight: 1,
  itemType: '',
  remark: ''
})

const discountText = computed(() => {
  if (!selectedMember.value || !selectedMember.value.discount) return '无折扣'
  const d = selectedMember.value.discount
  if (d >= 100) return '无折扣'
  return `${(d / 10).toFixed(1)}折`
})

const rules = {
  senderName: [
    { required: true, message: '请输入寄件人姓名', trigger: 'blur' }
  ],
  senderPhone: [
    { required: true, message: '请输入寄件人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverName: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入收件人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收件地址', trigger: 'blur' },
    { min: 10, message: '请输入详细地址', trigger: 'blur' }
  ],
  companyId: [
    { required: true, message: '请选择快递公司', trigger: 'change' }
  ],
  weight: [
    { required: true, message: '请输入物品重量', trigger: 'blur' }
  ]
}

const loadCompanies = async () => {
  try {
    const res = await getCompanies({ enabled: true })
    if (Array.isArray(res)) {
      companies.value = res
    }
  } catch (e) {}
}

const handleCompanyChange = async (companyId) => {
  form.templateId = null
  templates.value = []
  priceInfo.originalFreight = null
  priceInfo.discountAmount = null
  priceInfo.finalFreight = null

  if (companyId) {
    try {
      const res = await getTemplates({ companyId, enabled: true })
      if (Array.isArray(res)) {
        templates.value = res
        const defaultTpl = res.find(t => t.isDefault)
        if (defaultTpl) {
          form.templateId = defaultTpl.id
        }
      }
    } catch (e) {}
    calculatePrice()
  }
}

const calculatePrice = async () => {
  if (!form.companyId || !form.weight || form.weight <= 0) {
    priceInfo.originalFreight = null
    priceInfo.discountAmount = null
    priceInfo.finalFreight = null
    return
  }

  try {
    const data = {
      companyId: form.companyId,
      weight: form.weight
    }
    if (form.templateId) {
      data.templateId = form.templateId
    }
    const res = await apiCalculatePrice(data)
    const original = parseFloat(res.totalPrice)
    priceInfo.originalFreight = original.toFixed(2)

    if (selectedMember.value && selectedMember.value.discount && selectedMember.value.discount < 100) {
      const discountRate = selectedMember.value.discount / 100
      const finalPrice = original * discountRate
      priceInfo.discountAmount = (original - finalPrice).toFixed(2)
      priceInfo.finalFreight = finalPrice.toFixed(2)
    } else {
      priceInfo.discountAmount = null
      priceInfo.finalFreight = original.toFixed(2)
    }
  } catch (e) {
    priceInfo.originalFreight = null
    priceInfo.discountAmount = null
    priceInfo.finalFreight = null
  }
}

const searchMembers = async (query) => {
  if (!query) {
    memberOptions.value = []
    return
  }
  memberLoading.value = true
  try {
    const res = await getMembers({ name: query })
    if (Array.isArray(res)) {
      memberOptions.value = res
    }
  } catch (e) {
    memberOptions.value = []
  } finally {
    memberLoading.value = false
  }
}

const handleMemberChange = (memberId) => {
  if (memberId) {
    const member = memberOptions.value.find(m => m.id === memberId)
    if (member) {
      selectedMember.value = member
      form.senderName = member.name
      form.senderPhone = member.phone
    }
  } else {
    selectedMember.value = null
  }
  calculatePrice()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await createShipment(form)
        ElMessage.success('代寄件登记成功')
        handleReset()
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '代寄件登记失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.memberId = null
  form.companyId = null
  form.templateId = null
  form.weight = 1
  form.itemType = ''
  form.remark = ''
  selectedMember.value = null
  memberOptions.value = []
  templates.value = []
  priceInfo.originalFreight = null
  priceInfo.discountAmount = null
  priceInfo.finalFreight = null
}

onMounted(() => {
  loadCompanies()
})
</script>

<style scoped>
.send-register {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.price-preview {
  width: 100%;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.price-value {
  font-weight: 500;
  color: #303133;
}

.price-save {
  font-weight: 500;
  color: #67c23a;
}

.price-final-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.price-final {
  font-size: 22px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
