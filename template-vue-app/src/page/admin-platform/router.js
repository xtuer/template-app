import Vue from 'vue';
import Router from 'vue-router';
import Main from '@/page/admin-platform/main';

Vue.use(Router);

export default new Router({
    routes: [{
        path: '/',
        name: 'main',
        redirect: '/school',
        component: Main,
        children: [{
                path: 'school',
                name: 'school',
                component: () => import('./subpage/school/school.vue')
            },
            {
                path: 'school-meta',
                name: 'school-meta',
                component: () => import('./subpage/school/school-meta.vue')
            },
            {
                path: 'school-editor/:id',
                name: 'school-editor',
                component: () => import('./subpage/school/school-editor.vue')
            }
        ]
    }],
});
