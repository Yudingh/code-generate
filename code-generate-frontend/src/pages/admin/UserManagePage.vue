<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { deleteUsingPost, listUserVoByPage, update } from '@/api/userController.ts'

const loading = ref(false)
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

const pagination = reactive({
  current: 1,
  pageSize: 10,
})

const editModalVisible = ref(false)
const editLoading = ref(false)
const currentUserAccount = ref('')
const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

const searchForm = reactive({
  userAccount: '',
  userName: '',
})

const roleTextMap: Record<string, string> = {
  admin: '管理员',
  user: '用户',
}

const formatDateTime = (value?: string) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const columns = computed(() => [
  {
    title: '用户ID',
    dataIndex: 'id',
    key: 'id',
    width: 180,
    customCell: () => ({ style: { whiteSpace: 'nowrap' } }),
  },
  {
    title: '用户账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    ellipsis: true,
    width: 150,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
  },
  {
    title: '用户头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    width: 100,
  },
  {
    title: '用户简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 110,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 140,
  },
])

const buildQueryParams = () => {
  const userAccount = searchForm.userAccount.trim()
  const userName = searchForm.userName.trim()
  const params: API.UserQueryRequest = {
    pageNum: pagination.current,
    pageSize: pagination.pageSize,
  }
  if (userAccount) {
    params.userAccount = userAccount
  }
  if (userName) {
    params.userName = userName
  }
  return params
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage(buildQueryParams())
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records || []
      total.value = Number(res.data.data.totalRow) || 0
      return
    }
    message.error(res.data.message || '加载用户数据失败')
  } catch (error: any) {
    message.error(error?.message || '加载用户数据失败')
  } finally {
    loading.value = false
  }
}

const onPageChange = async (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  await loadData()
}

const handleSearch = async () => {
  pagination.current = 1
  await loadData()
}

const handleEdit = (_record: API.UserVO) => {
  editForm.id = _record.id
  editForm.userName = _record.userName || ''
  editForm.userAvatar = _record.userAvatar || ''
  editForm.userProfile = _record.userProfile || ''
  editForm.userRole = _record.userRole || 'user'
  currentUserAccount.value = _record.userAccount || '-'
  editModalVisible.value = true
}

const handleEditCancel = () => {
  editModalVisible.value = false
}

const handleEditSubmit = async () => {
  if (!editForm.id) {
    message.error('用户ID不存在，无法更新')
    return
  }
  if (!editForm.userName?.trim()) {
    message.warning('用户名不能为空')
    return
  }
  if (!editForm.userRole) {
    message.warning('请选择用户角色')
    return
  }

  editLoading.value = true
  try {
    const res = await update({
      id: editForm.id,
      userName: editForm.userName.trim(),
      userAvatar: editForm.userAvatar?.trim(),
      userProfile: editForm.userProfile?.trim(),
      userRole: editForm.userRole,
    })
    if (res.data.code === 0) {
      message.success('修改成功')
      editModalVisible.value = false
      await loadData()
      return
    }
    message.error(res.data.message || '修改失败')
  } catch (error: any) {
    message.error(error?.message || '修改失败')
  } finally {
    editLoading.value = false
  }
}

const handleDelete = async (_record: API.UserVO) => {
  if (!_record.id) {
    message.error('用户ID不存在，无法删除')
    return
  }
  try {
    const res = await deleteUsingPost({ userId: _record.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      if (dataList.value.length === 1 && pagination.current > 1) {
        pagination.current -= 1
      }
      await loadData()
      return
    }
    message.error(res.data.message || '删除失败')
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<template>
  <div class="user-manage-page">
    <a-card title="用户管理" :bordered="false">
      <a-form class="search-form" layout="inline" :model="searchForm" @finish="handleSearch">
        <a-form-item label="用户账号" name="userAccount">
          <a-input
            v-model:value="searchForm.userAccount"
            placeholder="请输入用户账号"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="searchForm.userName" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="dataList"
        :loading="loading"
        :pagination="false"
        :row-key="(record: API.UserVO) => record.id || 0"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-avatar :src="record.userAvatar">
              {{ (record.userName || record.userAccount || 'U').charAt(0) }}
            </a-avatar>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'blue' : 'default'">
              {{ roleTextMap[record.userRole || 'user'] || '用户' }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" class="action-btn edit-btn" @click="handleEdit(record)">
                修改
              </a-button>
              <a-popconfirm title="确认删除该用户吗？" @confirm="handleDelete(record)">
                <a-button size="small" class="action-btn delete-btn">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
          <template v-else>
            {{ record[column.dataIndex as keyof API.UserVO] || '-' }}
          </template>
        </template>
      </a-table>

      <div class="pagination-wrapper">
        <a-pagination
          :current="pagination.current"
          :page-size="pagination.pageSize"
          :total="total"
          :show-size-changer="true"
          :show-total="(v: number) => `共 ${v} 条`"
          @change="onPageChange"
        />
      </div>
    </a-card>

    <a-modal
      v-model:open="editModalVisible"
      title="修改用户"
      :confirm-loading="editLoading"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
    >
      <a-form layout="vertical" :model="editForm">
        <a-form-item label="用户账号">
          <a-input :value="currentUserAccount" disabled />
        </a-form-item>
        <a-form-item label="用户名" required>
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="用户头像">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像地址" />
        </a-form-item>
        <a-form-item label="用户简介">
          <a-textarea v-model:value="editForm.userProfile" :rows="3" placeholder="请输入用户简介" />
        </a-form-item>
        <a-form-item label="用户角色" required>
          <a-select
            v-model:value="editForm.userRole"
            :options="[
              { label: '管理员', value: 'admin' },
              { label: '用户', value: 'user' },
            ]"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-manage-page {
  padding: 12px 0;
}

.search-form {
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.action-btn {
  background: #fff;
}

.edit-btn {
  color: #52c41a;
  border-color: #52c41a;
}

.edit-btn:hover,
.edit-btn:focus {
  color: #73d13d;
  border-color: #73d13d;
}

.delete-btn {
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.delete-btn:hover,
.delete-btn:focus {
  color: #ff7875;
  border-color: #ff7875;
}
</style>
