import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
    history: createWebHashHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/about',
            name: 'about',
            // route level code-splitting
            // this generates a separate chunk (About.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import('../views/AboutView.vue')
        },
        {
            path: '/sql-ast',
            name: 'sql-ast',
            component: () => import('../views/SqlAst.vue')
        },
        {
            path: '/sql-editor',
            name: 'sql-editor',
            component: () => import('../views/SqlEditor.vue')
        },
    ]
})

export default router
