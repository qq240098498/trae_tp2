<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="28"><Box /></el-icon>
        <span class="logo-text">快递代收系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/package-in">
          <el-icon><Download /></el-icon>
          <span>包裹入库</span>
        </el-menu-item>
        <el-menu-item index="/package-pickup">
          <el-icon><Goods /></el-icon>
          <span>客户取件</span>
        </el-menu-item>
        <el-menu-item index="/package-query">
          <el-icon><Search /></el-icon>
          <span>包裹查询</span>
        </el-menu-item>
        <el-menu-item index="/滞留管理">
          <el-icon><Warning /></el-icon>
          <span>滞留管理</span>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/寄件登记">
          <el-icon><Edit /></el-icon>
          <span>代寄件登记</span>
        </el-menu-item>
        <el-menu-item index="/寄件管理">
          <el-icon><List /></el-icon>
          <span>代寄件管理</span>
        </el-menu-item>
        <el-sub-menu index="pricing">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>价格管理</span>
          </template>
          <el-menu-item index="/express-company">
            <el-icon><OfficeBuilding /></el-icon>
            <span>快递公司维护</span>
          </el-menu-item>
          <el-menu-item index="/price-template">
            <el-icon><Tickets /></el-icon>
            <span>价格模板维护</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="exception">
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>异常管理</span>
          </template>
          <el-menu-item index="/exception-manage">
            <el-icon><Warning /></el-icon>
            <span>异常登记</span>
          </el-menu-item>
          <el-menu-item index="/compensation">
            <el-icon><Money /></el-icon>
            <span>补偿管理</span>
          </el-menu-item>
          <el-menu-item index="/compensation-standard">
            <el-icon><Coin /></el-icon>
            <span>补偿金额标准</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <el-icon size="20"><User /></el-icon>
          <span>管理员</span>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '快递代收点管理系统')
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  background-color: #2b2f3a;
}

.logo-text {
  font-size: 16px;
  font-weight: bold;
}

.menu {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
