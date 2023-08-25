import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import mitt from 'mitt';
import axios from 'axios';

// 引入项目里的工具常量、工具类和比较小的静态文件。
import '@/static/ts/axios.rest.js';
import XIcon from '@/components/XIcon.vue';

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.component('XIcon', XIcon)

// 相当于 Event Bus。
const emitter = mitt();
app.config.globalProperties.emitter = emitter;

app.mount('#app')

// Axios 拦截器，注入认证 token。
axios.interceptors.request.use(
    (config) => {
        const token = 'your_token_here'; // Replace this with the actual token you want to use

        if (token) {
            config.headers['X-Auth-Token'] = token;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);
