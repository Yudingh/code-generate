<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/userController.ts'

const router = useRouter()
const loading = ref(false)

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const accountLength = computed(() => formState.userAccount?.length ?? 0)
const passwordLength = computed(() => formState.userPassword?.length ?? 0)
const checkPasswordLength = computed(() => formState.checkPassword?.length ?? 0)
const accountValid = computed(() => accountLength.value >= 6)
const passwordValid = computed(() => passwordLength.value >= 8)
const checkPasswordValid = computed(
  () => !!formState.checkPassword && formState.userPassword === formState.checkPassword,
)

const validateCheckPassword = async (_rule: unknown, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入密码')
  }
  if (value.length < 8) {
    return Promise.reject('确认密码长度需要大于等于 8 位')
  }
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 6, message: '账号长度需要大于等于 6 位', trigger: 'change' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度需要大于等于 8 位', trigger: 'change' },
  ],
  checkPassword: [{ validator: validateCheckPassword, trigger: 'change' }],
}

const handleFinish = async () => {
  loading.value = true
  try {
    const res = await register(formState)
    if (res.data.code === 0) {
      message.success('注册成功，请登录')
      await router.push('/user/login')
      return
    }
    message.error(res.data.message || '注册失败')
  } catch (error: any) {
    message.error(error?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="user-register-page">
    <a-card title="用户注册" class="register-card" :bordered="false">
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

        <a-form-item label="确认密码" name="checkPassword">
          <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">注册</a-button>
        </a-form-item>
      </a-form>

      <div class="tips">
        已有账号？
        <a @click.prevent="router.push('/user/login')">点击登录</a>
      </div>

      <div class="status-preview">
        <span>账号位数：{{ accountLength }}（{{ accountValid ? '满足' : '未满足' }}）</span>
        <span>密码位数：{{ passwordLength }}（{{ passwordValid ? '满足' : '未满足' }}）</span>
        <span>
          确认密码位数：{{ checkPasswordLength }}（{{ checkPasswordValid ? '一致' : '不一致' }}）
        </span>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.user-register-page {
  display: flex;
  justify-content: center;
  padding: 24px 12px;
}

.register-card {
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
