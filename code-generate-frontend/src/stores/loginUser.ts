import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLogin } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const defaultLoginUser: API.LoginUserVO = {
    userName: '未登录',
  }
  const loginUser = ref<API.LoginUserVO>({ ...defaultLoginUser })

  // 获取登录用户信息
  async function fetchLoginUser() {
    const res = await getLogin()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
      return
    }
    loginUser.value = { ...defaultLoginUser }
  }
  // 更新登录用户信息
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
