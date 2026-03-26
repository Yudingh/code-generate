<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import ACCESS_ENUM from '@/access/accessEnum'
import checkAccess from '@/access/checkAccess'
import { getCurrentAccess } from '@/access'
import logo from '@/assets/logo.svg'
import { logout } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

interface MenuItemConfig {
  key: string
  label: string
  path: string
  access?: string
}

const props = withDefaults(
  defineProps<{
    title?: string
    menuItems?: MenuItemConfig[]
  }>(),
  {
    title: '代码生成平台',
    menuItems: () => [
      { key: 'home', label: '首页', path: '/', access: ACCESS_ENUM.NOT_LOGIN },
    ],
  },
)

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const currentAccess = computed(() => getCurrentAccess(loginUserStore.loginUser))

const visibleMenuItems = computed(() =>
  props.menuItems.filter((item) => checkAccess(currentAccess.value, item.access || ACCESS_ENUM.NOT_LOGIN)),
)

const selectedKeys = computed(() => {
  const activeItem =
    visibleMenuItems.value.find(
      (item) => route.path === item.path || route.path.startsWith(`${item.path}/`),
    ) ?? visibleMenuItems.value[0]

  return activeItem ? [activeItem.key] : []
})

const menuOptions = computed(() =>
  visibleMenuItems.value.map((item) => ({
    key: item.key,
    label: item.label,
  })),
)

const handleMenuClick = ({ key }: { key: string }) => {
  const target = visibleMenuItems.value.find((item) => item.key === key)
  if (target && target.path !== route.path) {
    router.push(target.path)
  }
}

const isLoggedIn = computed(() => {
  return !!loginUserStore.loginUser?.id
})

const displayUsername = computed(() => {
  return (
    loginUserStore.loginUser?.userName ||
    (loginUserStore.loginUser as API.LoginUserVO & { username?: string })?.username ||
    '用户'
  )
})

const displayUserAvatar = computed(() => loginUserStore.loginUser?.userAvatar || '')

const userMenuItems = [{ key: 'logout', label: '退出登录' }]

const handleUserMenuClick = async ({ key }: { key: string }) => {
  if (key !== 'logout') {
    return
  }

  try {
    const res = await logout()
    if (res.data.code !== 0) {
      message.error(res.data.message || '退出登录失败')
      return
    }
    message.success('已退出登录')
  } catch (error: any) {
    message.error(error?.message || '退出登录失败')
    return
  }

  loginUserStore.setLoginUser({ userName: '未登录' })
  await router.push('/user/login')
}

</script>

<template>
  <a-layout-header class="global-header">
    <div class="brand">
      <img :src="logo" alt="logo" class="logo" />
      <span class="title">{{ title }}</span>
    </div>

    <a-menu
      class="menu"
      mode="horizontal"
      :selected-keys="selectedKeys"
      :items="menuOptions"
      @click="handleMenuClick"
    />

    <div class="user-area">
      <RouterLink v-if="!isLoggedIn" to="/user/login">
        <a-button type="primary">登录</a-button>
      </RouterLink>
      <a-dropdown v-else :trigger="['hover']">
        <div class="user-info">
          <a-avatar :src="displayUserAvatar">{{ displayUsername.charAt(0) }}</a-avatar>
          <span class="username">{{ displayUsername }}</span>
        </div>
        <template #overlay>
          <a-menu :items="userMenuItems" @click="handleUserMenuClick" />
        </template>
      </a-dropdown>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 16px;
  height: 64px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.brand {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
  min-width: 180px;
}

.logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  white-space: nowrap;
}

.menu {
  flex: 1;
  min-width: 0;
  border-bottom: none;
}

.user-area {
  position: relative;
  z-index: 2;
  display: flex;
  flex: 0 0 auto;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #1f1f1f;
  font-size: 14px;
}

@media (max-width: 768px) {
  .global-header {
    gap: 8px;
    padding: 0 12px;
  }

  .brand {
    min-width: auto;
  }

  .title {
    display: none;
  }
}
</style>
