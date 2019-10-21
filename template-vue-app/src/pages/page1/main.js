import { LoadingBar, Message, Notice } from 'view-design';
import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import '@/plugins/iview';

import filters from '@/../public/static/js/filters';
import methods from '@/../public/static/js/methods';
import UserDao from '@/../public/static/js/dao/UserDao';

// [1] 注册 Vue 的原型函数
Object.keys(methods).forEach((key) => {
    Vue.prototype[key] = methods[key];
});

// [2] 注册全局过滤器
Object.keys(filters).forEach(key => {
    Vue.filter(key, filters[key]);
});

// [3] 注册 Dao
Vue.prototype.$UserDao = UserDao;

// [4] 注册全局组件
// Vue.component('ProjectTable', ProjectTable);

// [5] 定义全局的 Message, Notice，为了能够在 Dao 中使用
window.Message = Message;
window.Notice  = Notice;

Vue.config.productionTip = false;

// [6] 获取登录用户
router.beforeEach((to, from, next) => {
    if (store.getters.logined) {
        // 已登录，继续访问
        next();
    } else {
        // 未登录，从服务器获取当前登录用户信息
        LoadingBar.start();
        UserDao.findCurrentUser().then(user => {
            store.commit('setUser', user);
            next();
            LoadingBar.finish();
        }).catch(error => {
            LoadingBar.error();
        });
    }
});

new Vue({
    router,
    store,
    render: h => h(App),
}).$mount('#app');
