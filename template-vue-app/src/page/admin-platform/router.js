import Vue from 'vue';
import Router from 'vue-router';
import Main from '@/page/admin-platform/main';

Vue.use(Router);

export default new Router({
    routes: [{
        path: '/',
        redirect: '/school',
        component: Main,
        children: [{
                path: 'school',
                name: 'school',
                component: () => import('./subpage/school/school.vue')
            },
            {
                path: 'dict',
                name: 'dict',
                component: () => import('./subpage/school/dict.vue')
            },
            {
                path: 'school-editor/:id',
                name: 'school-editor',
                component: () => import('./subpage/school/school-editor.vue')
            }
        ]
    }],
});
