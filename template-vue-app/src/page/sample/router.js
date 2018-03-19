import Vue from 'vue';
import Router from 'vue-router';
import Main from '@/page/sample/main';

Vue.use(Router);

export default new Router({
    routes: [{
        path: '/',
        component: Main,
        children: []
    }],
});
