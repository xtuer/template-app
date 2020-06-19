import Vue from 'vue';

import App from './App.vue';
import router from './router';
import store from './store';
import './init';

router.beforeEach((to, from, next) => {
    if (store.getters.logined) {
        // 已登录，继续访问 (如有需要，还要进行权限判断)
        next();
    } else if (to.path === '/login') {
        // 访问登录页不需要权限
        next();
    } else {
        // 未登录，请求登录信息，如果未登录则访问登录页
        store.dispatch('loadCurrentUser').then(() => {
            next();
        }).catch(() => {
            next('/login');
        });
    }
});

Vue.config.productionTip = false;

new Vue({
    router,
    store,
    render: h => h(App),
}).$mount('#app');
