<script setup lang="ts">
import { RouterView } from 'vue-router'
import ACCESS_ENUM from '@/access/accessEnum'
import GlobalFooter from '@/components/GlobalFooter.vue'
import GlobalHeader from '@/components/GlobalHeader.vue'

const menuItems = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'userManage', label: '用户管理', path: '/admin/userManage', access: ACCESS_ENUM.ADMIN },
]
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader :menu-items="menuItems" />
    <a-layout-content class="layout-content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.layout-content {
  flex: 1;
  padding: 20px;
}

.content-wrapper {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 12px;
  }
}
</style>
