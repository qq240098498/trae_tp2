<template>
  <div class="member-manage">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>会员管理</span>
          <el-button type="primary" size="small" @click="handleAdd" style="margin-left: auto">
            <el-icon><Plus /></el-icon>
            新增会员
          </el-button>
        </div>
      </template>

      <div style="margin-bottom: 16px; display: flex; gap: 12px;">
        <el-input
          v-model="searchName"
          placeholder="按姓名搜索会员"
          clearable
          style="width: 250px"
          @clear="loadData"
          @keyup.enter="loadData"
        >
          <template #append>
            <el-button @click="loadData">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column label="寄件折扣" width="120">
          <template #default="{ row }">
            <el-tag type="danger" size="small" v-if="row.discount">
              {{ row.discount }}%
            </el-tag>
            <el-tag type="info" size="small" v-else>无折扣</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会员' : '新增会员'"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入会员姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址（可选）" />
        </el-form-item>
        <el-form-item label="寄件折扣(%)" prop="discount">
          <el-input-number
            v-model="form.discount"
            :min="1"
            :max="100"
            :step="1"
            style="width: 100%"
            placeholder="折扣百分比，如90表示9折"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">
            折扣百分比：90表示原价的90%（即9折），100表示无折扣
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMembers, createMember, updateMember, deleteMember } from '@/api/member'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const searchName = ref('')

const tableData = ref([])

const form = reactive({
  id: null,
  name: '',
  phone: '',
  address: '',
  discount: 100
})

const rules = {
  name: [{ required: true, message: '请输入会员姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchName.value) {
      params.name = searchName.value
    }
    const res = await getMembers(params)
    if (Array.isArray(res)) {
      tableData.value = res
    }
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.discount = 100
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.phone = row.phone
  form.address = row.address || ''
  form.discount = row.discount || 100
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除会员「${row.name}」？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteMember(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.phone = ''
  form.address = ''
  form.discount = 100
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = {
          name: form.name,
          phone: form.phone,
          address: form.address,
          discount: form.discount
        }
        if (isEdit.value) {
          await updateMember(form.id, data)
          ElMessage.success('修改成功')
        } else {
          await createMember(data)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (e) {
        ElMessage.error(e.response?.data?.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.member-manage {
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
