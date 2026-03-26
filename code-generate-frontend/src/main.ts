import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import setupAccess from '@/access'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

const app = createApp(App)
const pinia = createPinia()

setupAccess(router, pinia)

app.use(pinia)
app.use(router)
app.use(Antd)

app.mount('#app')
