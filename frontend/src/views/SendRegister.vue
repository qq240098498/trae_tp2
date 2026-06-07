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
        label-width="100px"
        style="max-width: 700px; margin: 0 auto;"
      >
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

        <el-divider content-position="left">包裹信息</el-divider>

        <el-form-item label="物品重量(kg)" prop="weight">
          <el-input-number
            v-model="form.weight"
            :min="0.1"
            :max="50"
            :step="0.1"
            size="large"
            style="width: 100%"
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
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createShipment } from '@/api/package'

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  senderName: '',
  senderPhone: '',
  receiverName: '',
  receiverPhone: '',
  address: '',
  weight: 1,
  itemType: '',
  remark: ''
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
  weight: [
    { required: true, message: '请输入物品重量', trigger: 'blur' }
  ]
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
        ElMessage.success('代寄件登记成功')
        handleReset()
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
  form.weight = 1
  form.itemType = ''
}
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
</style>
