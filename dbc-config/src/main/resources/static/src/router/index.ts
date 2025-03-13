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
    },
    {
        path: '/config/file',
        component: () => import((`@/views/dbc/ConfigFile.vue`))
    },
    {
        path: '/config/query',
        component: () => import((`@/views/dbc/ConfigQuery.vue`))
    },
    {
        path: '/back/data',
        component: () => import((`@/views/dbc/BackData.vue`))
    },
    {
        path: '/config/compare',
        component: () => import((`@/views/dbc/ConfigCompare.vue`))
    },
    {
        path: '/config/compareBackup',
        component: () => import((`@/views/dbc/ConfigCompareBackup.vue`))
    },
    {
        path: '/guide',
        component: () => import((`@/views/dbc/Guide.vue`))
    }
]

const router = createRouter({
    // history: createWebHistory(process.env.BASE_URL),
    history: createWebHashHistory(process.env.BASE_URL),
    routes
})

export default router