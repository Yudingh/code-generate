<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import logo from '@/assets/logo.svg'

interface MenuItemConfig {
  key: string
  label: string
  path: string
}

const props = withDefaults(
  defineProps<{
    title?: string
    menuItems?: MenuItemConfig[]
  }>(),
  {
    title: '代码生成平台',
    menuItems: () => [
      { key: 'home', label: '首页', path: '/' },
      { key: 'about', label: '关于', path: '/about' },
    ],
  },
)

const route = useRoute()
const router = useRouter()

const selectedKeys = computed(() => {
  const activeItem =
    props.menuItems.find((item) => route.path === item.path || route.path.startsWith(`${item.path}/`)) ??
    props.menuItems[0]

  return activeItem ? [activeItem.key] : []
})

const menuOptions = computed(() =>
  props.menuItems.map((item) => ({
    key: item.key,
    label: item.label,
  })),
)

const handleMenuClick = ({ key }: { key: string }) => {
  const target = props.menuItems.find((item) => item.key === key)
  if (target && target.path !== route.path) {
    router.push(target.path)
  }
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
      <a-button type="primary">登录</a-button>
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
  display: flex;
  flex: 0 0 auto;
  align-items: center;
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
