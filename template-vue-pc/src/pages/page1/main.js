import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import '@/plugins/iview';
import './init';

Vue.config.productionTip = false;

// 获取登录用户
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
        }).catch(() => {
            LoadingBar.error();
        });
    }
});

new Vue({
    router,
    store,
    render: h => h(App),
}).$mount('#app');
