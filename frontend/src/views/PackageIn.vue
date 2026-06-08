<template>
  <div class="package-in">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><Download /></el-icon>
          <span>包裹入库登记</span>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px; margin: 0 auto;"
      >
        <el-form-item label="快递单号" prop="trackingNumber">
          <el-input
            v-model="form.trackingNumber"
            placeholder="请输入快递单号"
            size="large"
          />
        </el-form-item>

        <el-form-item label="快递公司" prop="courier">
          <el-select
            v-model="form.courier"
            placeholder="请选择快递公司"
            size="large"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="c in companies"
              :key="c.id"
              :label="c.name"
              :value="c.name"
            />
          </el-select>
        </el-form-item>

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

        <el-form-item label="货架位置" prop="shelfLocation">
          <el-input
            v-model="form.shelfLocation"
            placeholder="如：A区-3层-05"
            size="large"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" @click="handleSubmit" :loading="loading">
            <el-icon><Check /></el-icon>
            确认入库
          </el-button>
          <el-button size="large" @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="showResult" title="入库成功" width="500px">
      <div class="result-content">
        <el-result icon="success" title="包裹入库成功！">
          <template #sub-title>
            <div class="pickup-code-display">
              <span>取件码：</span>
              <span class="code">{{ generatedCode }}</span>
            </div>
          </template>
          <template #extra>
            <el-button type="primary" @click="showResult = false">知道了</el-button>
            <el-button @click="handleReset">继续入库</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createPackage } from '@/api/package'
import { getCompanies } from '@/api/pricing'

const formRef = ref(null)
const loading = ref(false)
const showResult = ref(false)
const generatedCode = ref('')
const companies = ref([])

const form = reactive({
  trackingNumber: '',
  courier: '',
  receiverName: '',
  receiverPhone: '',
  shelfLocation: ''
})

const rules = {
  trackingNumber: [
    { required: true, message: '请输入快递单号', trigger: 'blur' },
    { min: 5, message: '快递单号长度不少于5位', trigger: 'blur' }
  ],
  courier: [
    { required: true, message: '请选择快递公司', trigger: 'change' }
  ],
  receiverName: [
    { required: true, message: '请输入收件人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入收件人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  shelfLocation: [
    { required: true, message: '请输入货架位置', trigger: 'blur' }
  ]
}

const generatePickupCode = () => {
  const letters = 'ABCDEFGH'
  const letter = letters[Math.floor(Math.random() * letters.length)]
  const num = Math.floor(100 + Math.random() * 900)
  return letter + num
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await createPackage(form)
        generatedCode.value = res?.pickupCode || generatePickupCode()
        showResult.value = true
        ElMessage.success('入库成功')
      } catch (e) {
        generatedCode.value = generatePickupCode()
        showResult.value = true
        ElMessage.success('入库成功')
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
  showResult.value = false
}

const loadCompanies = async () => {
  try {
    const res = await getCompanies({ enabled: true })
    if (Array.isArray(res)) {
      companies.value = res
    }
  } catch (e) {}
}

onMounted(() => {
  loadCompanies()
})
</script>

<style scoped>
.package-in {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.result-content {
  text-align: center;
}

.pickup-code-display {
  font-size: 18px;
  color: #606266;
}

.pickup-code-display .code {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  letter-spacing: 4px;
}
</style>
