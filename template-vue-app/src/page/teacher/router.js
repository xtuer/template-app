import Vue from 'vue';
import Router from 'vue-router';
import Main from '@/page/teacher/main';

Vue.use(Router);

export default new Router({
    routes: [{
        path: '/',
        component: Main,
        children: []
    }],
});
