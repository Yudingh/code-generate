<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { login } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const accountLength = computed(() => formState.userAccount?.length ?? 0)
const passwordLength = computed(() => formState.userPassword?.length ?? 0)
const accountValid = computed(() => accountLength.value >= 6)
const passwordValid = computed(() => passwordLength.value >= 8)

const rules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 6, message: '账号长度需要大于等于 6 位', trigger: 'change' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度需要大于等于 8 位', trigger: 'change' },
  ],
}

const handleFinish = async () => {
  loading.value = true
  try {
    const res = await login(formState)
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
      message.success('登录成功')
      await router.push('/')
      return
    }
    message.error(res.data.message || '登录失败')
  } catch (error: any) {
    message.error(error?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="user-login-page">
    <a-card title="用户登录" class="login-card" :bordered="false">
      <a-form
        layout="vertical"
        :model="formState"
        :rules="rules"
        @finish="handleFinish"
        autocomplete="off"
      >
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>

        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        </a-form-item>
      </a-form>

      <div class="tips">
        没有账号？
        <a @click.prevent="router.push('/user/register')">点击注册</a>
      </div>

      <div class="status-preview">
        <span>账号位数：{{ accountLength }}（{{ accountValid ? '满足' : '未满足' }}）</span>
        <span>密码位数：{{ passwordLength }}（{{ passwordValid ? '满足' : '未满足' }}）</span>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.user-login-page {
  display: flex;
  justify-content: center;
  padding: 24px 12px;
}

.login-card {
  width: 100%;
  max-width: 420px;
}

.tips {
  margin-top: 8px;
  text-align: center;
}

.status-preview {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 12px;
  color: rgba(0, 0, 0, 0.65);
  font-size: 12px;
}
</style>
