import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 引入项目里的工具常量、工具类和比较小的静态文件。
import '@/static/js/axios.rest.js';

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
