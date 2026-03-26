<template>
  <div class="no-auth-page">
    <a-result
      status="403"
      title="401"
      sub-title="抱歉，您暂无访问该页面的权限。"
    >
      <template #extra>
        <a-button type="primary" @click="$router.push('/')">返回首页</a-button>
      </template>
    </a-result>
  </div>
</template>

<style scoped>
.no-auth-page {
  padding: 24px 0;
}
</style>
