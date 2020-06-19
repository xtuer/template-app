import Vue from 'vue';
import Vant, { Lazyload, Toast } from 'vant';
import dayjs from 'dayjs';
import 'vant/lib/index.css';

import '@/../public/static-m/js/urls';
import '@/../public/static-m/js/utils';
import '@/../public/static-m/js/constants';
import '@/../public/static-m/lib/axios.rest';

import filters from '@/../public/static-m/js/filters';
import methods from '@/../public/static-m/js/methods';

import Navigator from '@/components/Navigator.vue';

// [2] 注册组件
Vue.use(Vant);
Vue.use(Lazyload);
Vue.component('Navigator', Navigator);

// [3] 注册全局过滤器
Object.keys(filters).forEach(key => {
    Vue.filter(key, filters[key]);
});

// [4] 注册 Vue 的原型函数
Object.keys(methods).forEach(key => {
    Vue.prototype[key] = methods[key];
});

// [5] 其他
Vue.prototype.window = window;
window.dayjs = dayjs;
window.Toast = Toast;
