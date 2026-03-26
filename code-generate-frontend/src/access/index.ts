import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'
import { useLoginUserStore } from '@/stores/loginUser.ts'

let hasAutoLogin = false

export const getCurrentAccess = (loginUser?: API.LoginUserVO) => {
  if (!loginUser?.id) {
    return ACCESS_ENUM.NOT_LOGIN
  }
  if (loginUser.userRole === ACCESS_ENUM.ADMIN) {
    return ACCESS_ENUM.ADMIN
  }
  return ACCESS_ENUM.USER
}

const setupAccess = (router: Router, pinia: Pinia) => {
  router.beforeEach(async (to) => {
    const loginUserStore = useLoginUserStore(pinia)

    // 首次进入页面时自动尝试获取登录态
    if (!hasAutoLogin && !loginUserStore.loginUser?.id) {
      hasAutoLogin = true
      try {
        await loginUserStore.fetchLoginUser()
      } catch (error) {
        // 忽略自动登录失败，后续按权限逻辑处理
      }
    }

    const needAccess = (to.meta?.access as string) || ACCESS_ENUM.NOT_LOGIN
    if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
      return true
    }

    const currentAccess = getCurrentAccess(loginUserStore.loginUser)
    if (!checkAccess(currentAccess, ACCESS_ENUM.USER)) {
      return {
        path: '/user/login',
        query: { redirect: to.fullPath },
      }
    }

    if (needAccess === ACCESS_ENUM.ADMIN && !checkAccess(currentAccess, ACCESS_ENUM.ADMIN)) {
      return {
        path: '/401',
      }
    }

    return true
  })
}

export default setupAccess
