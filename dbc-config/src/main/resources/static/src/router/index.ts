import {createRouter, createWebHashHistory} from 'vue-router'

const routes = [
    {
        path: '',
        component: () => import((`@/views/dbc/service.vue`))
    },
    {
        path: '/service',
        component: () => import((`@/views/dbc/service.vue`))
    },
    {
        path: '/profile',
        component: () => import((`@/views/dbc/profile.vue`))
    },
    {
        path: '/config/profile',
        component: () => import((`@/views/dbc/ConfigProfile.vue`))
    },
    {
        path: '/config/list',
        component: () => import((`@/views/dbc/ConfigList.vue`))
    }
]

const router = createRouter({
    // history: createWebHistory(process.env.BASE_URL),
    history: createWebHashHistory(process.env.BASE_URL),
    routes
})

export default router