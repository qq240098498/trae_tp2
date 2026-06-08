import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'package-in',
        name: 'PackageIn',
        component: () => import('@/views/PackageIn.vue'),
        meta: { title: '包裹入库' }
      },
      {
        path: 'package-pickup',
        name: 'PackagePickup',
        component: () => import('@/views/PackagePickup.vue'),
        meta: { title: '客户取件' }
      },
      {
        path: 'package-query',
        name: 'PackageQuery',
        component: () => import('@/views/PackageQuery.vue'),
        meta: { title: '包裹查询' }
      },
      {
        path: '滞留管理',
        name: '滞留管理',
        component: () => import('@/views/Overdue.vue'),
        meta: { title: '滞留管理' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: '寄件登记',
        name: '寄件登记',
        component: () => import('@/views/SendRegister.vue'),
        meta: { title: '代寄件登记' }
      },
      {
        path: '寄件管理',
        name: '寄件管理',
        component: () => import('@/views/SendManage.vue'),
        meta: { title: '代寄件管理' }
      },
      {
        path: 'express-company',
        name: 'ExpressCompany',
        component: () => import('@/views/ExpressCompany.vue'),
        meta: { title: '快递公司维护' }
      },
      {
        path: 'price-template',
        name: 'PriceTemplate',
        component: () => import('@/views/PriceTemplate.vue'),
        meta: { title: '价格模板维护' }
      },
      {
        path: 'exception-manage',
        name: 'ExceptionManage',
        component: () => import('@/views/ExceptionManage.vue'),
        meta: { title: '异常管理' }
      },
      {
        path: 'compensation',
        name: 'Compensation',
        component: () => import('@/views/Compensation.vue'),
        meta: { title: '补偿管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
