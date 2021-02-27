import Vue from 'vue';
import { LoadingBar, Message, Notice } from 'view-design';
import vuescroll from 'vuescroll';
import dayjs from 'dayjs';

import '@/../public/static-p/js/urls';
import '@/../public/static-p/js/utils';
import '@/../public/static-p/js/constants';
import '@/../public/static-p/lib/axios.rest';
import '@/../public/static-p/css/global.css';

import filters from '@/../public/static-p/js/filters';
import methods from '@/../public/static-p/js/methods';

// [1] 注册 Vue 的原型函数
Object.keys(methods).forEach((key) => {
    Vue.prototype[key] = methods[key];
});

// [2] 注册全局过滤器
Object.keys(filters).forEach(key => {
    Vue.filter(key, filters[key]);
});

// [3] 注册全局组件
// 滚动条
Vue.use(vuescroll, {
    ops: {
        bar: {
            background: '#c1c1c1',
        }
    },
    name: 'Scroll' // customize component name, default -> vueScroll
});

// Vue.component('ProjectTable', ProjectTable);

// [4] 定义全局变量: Message, Notice，为了能够在 Dao 中使用
window.LoadingBar = LoadingBar;
window.Message    = Message;
window.Notice     = Notice;

// 其他
window.dayjs = dayjs;
Vue.prototype.window = window;
