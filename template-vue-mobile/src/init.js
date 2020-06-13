// 引入所有 Vant 组件
// import Vant from 'vant';
// import 'vant/lib/index.css';
// Vue.use(Vant);

import Vue from 'vue';
import { Button, Toast, Field, CellGroup, Uploader } from 'vant';
import dayjs from 'dayjs';

import '@/../public/static-m/js/urls';
import '@/../public/static-m/js/utils';
import '@/../public/static-m/js/constants';
import '@/../public/static-m/lib/axios.rest';

import filters from '@/../public/static-m/js/filters';
import methods from '@/../public/static-m/js/methods';

Vue.use(Button);
Vue.use(Toast);
Vue.use(Field);
Vue.use(CellGroup);
Vue.use(Uploader);

// [3] 注册全局过滤器
Object.keys(filters).forEach(key => {
    Vue.filter(key, filters[key]);
});

// [4] 注册 Vue 的原型函数
Object.keys(methods).forEach(key => {
    Vue.prototype[key] = methods[key];
});

// 其他
window.dayjs = dayjs;
