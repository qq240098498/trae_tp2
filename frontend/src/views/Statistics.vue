<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span>最近7天每日入库量</span>
            </div>
          </template>

          <div class="chart-container">
            <div class="chart">
              <div class="chart-bars">
                <div
                  v-for="(item, index) in weeklyData"
                  :key="index"
                  class="bar-item"
                >
                  <div class="bar-wrapper">
                    <div
                      class="bar"
                      :style="{ height: getBarHeight(item.count) + '%' }"
                    >
                      <span class="bar-value">{{ item.count }}</span>
                    </div>
                  </div>
                  <div class="bar-label">{{ item.date }}</div>
                </div>
              </div>
              <div class="chart-y-axis">
                <span>100</span>
                <span>80</span>
                <span>60</span>
                <span>40</span>
                <span>20</span>
                <span>0</span>
              </div>
            </div>
          </div>

          <div class="stats-summary">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label">7天总入库</div>
                  <div class="summary-value">{{ totalCount }} 件</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label">日均入库</div>
                  <div class="summary-value">{{ avgCount }} 件</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="summary-item">
                  <div class="summary-label">最高日入库</div>
                  <div class="summary-value">{{ maxCount }} 件</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><PieChart /></el-icon>
              <span>快递公司占比</span>
            </div>
          </template>
          <div class="pie-chart">
            <div class="pie-item" v-for="(item, index) in companyData" :key="index">
              <div class="pie-legend">
                <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
                <span class="legend-name">{{ item.name }}</span>
              </div>
              <div class="pie-bar-wrapper">
                <div class="pie-bar" :style="{ width: item.percent + '%', backgroundColor: item.color }"></div>
              </div>
              <span class="pie-percent">{{ item.percent }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span>取件时效统计</span>
            </div>
          </template>
          <div class="timeline-stats">
            <div class="timeline-item">
              <div class="timeline-label">24小时内取件</div>
              <div class="timeline-bar">
                <div class="timeline-fill fill-1" style="width: 65%"></div>
              </div>
              <span class="timeline-value">65%</span>
            </div>
            <div class="timeline-item">
              <div class="timeline-label">24-48小时取件</div>
              <div class="timeline-bar">
                <div class="timeline-fill fill-2" style="width: 25%"></div>
              </div>
              <span class="timeline-value">25%</span>
            </div>
            <div class="timeline-item">
              <div class="timeline-label">超过48小时</div>
              <div class="timeline-bar">
                <div class="timeline-fill fill-3" style="width: 10%"></div>
              </div>
              <span class="timeline-value">10%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDailyStats } from '@/api/package'
import { TrendCharts, PieChart, DataLine } from '@element-plus/icons-vue'

const weeklyData = ref([
  { date: '01/09', count: 45 },
  { date: '01/10', count: 52 },
  { date: '01/11', count: 38 },
  { date: '01/12', count: 65 },
  { date: '01/13', count: 58 },
  { date: '01/14', count: 72 },
  { date: '01/15', count: 56 }
])

const companyData = ref([
  { name: '顺丰', count: 120, percent: 30, color: '#409EFF' },
  { name: '圆通', count: 80, percent: 20, color: '#67C23A' },
  { name: '中通', count: 100, percent: 25, color: '#E6A23C' },
  { name: '申通', count: 40, percent: 10, color: '#F56C6C' },
  { name: '其他', count: 60, percent: 15, color: '#909399' }
])

const maxCount = computed(() => {
  return Math.max(...weeklyData.value.map(item => item.count))
})

const totalCount = computed(() => {
  return weeklyData.value.reduce((sum, item) => sum + item.count, 0)
})

const avgCount = computed(() => {
  return Math.round(totalCount.value / weeklyData.value.length)
})

const getBarHeight = (count) => {
  return (count / 100) * 100
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${month}/${day}`
}

const loadData = async () => {
  try {
    const res = await getDailyStats()
    if (res && Array.isArray(res)) {
      weeklyData.value = res.map(item => ({
        date: formatDate(item.date),
        count: Number(item.count)
      }))
    } else if (res && res.dailyStats && Array.isArray(res.dailyStats)) {
      weeklyData.value = res.dailyStats.map(item => ({
        date: formatDate(item.date),
        count: Number(item.count)
      }))
    }
  } catch (e) {
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.chart-container {
  padding: 20px;
}

.chart {
  display: flex;
  align-items: flex-end;
  height: 300px;
  position: relative;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 250px;
  padding-right: 10px;
  color: #909399;
  font-size: 12px;
  border-right: 1px solid #ebeef5;
}

.chart-bars {
  flex: 1;
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 250px;
  padding: 0 20px;
  border-bottom: 1px solid #ebeef5;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.bar-wrapper {
  height: 250px;
  display: flex;
  align-items: flex-end;
}

.bar {
  width: 40px;
  background: linear-gradient(180deg, #409EFF 0%, #66b1ff 100%);
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.5s ease;
  display: flex;
  justify-content: center;
}

.bar-value {
  position: absolute;
  top: -20px;
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.bar-label {
  margin-top: 10px;
  font-size: 12px;
  color: #606266;
}

.stats-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.pie-chart {
  padding: 10px 20px;
}

.pie-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.pie-legend {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 80px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-name {
  font-size: 14px;
  color: #606266;
}

.pie-bar-wrapper {
  flex: 1;
  height: 20px;
  background-color: #f0f2f5;
  border-radius: 10px;
  overflow: hidden;
}

.pie-bar {
  height: 100%;
  border-radius: 10px;
  transition: width 0.5s ease;
}

.pie-percent {
  width: 50px;
  text-align: right;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.timeline-stats {
  padding: 20px;
}

.timeline-item {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  gap: 15px;
}

.timeline-label {
  width: 120px;
  font-size: 14px;
  color: #606266;
}

.timeline-bar {
  flex: 1;
  height: 24px;
  background-color: #f0f2f5;
  border-radius: 12px;
  overflow: hidden;
}

.timeline-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.5s ease;
}

.fill-1 {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.fill-2 {
  background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%);
}

.fill-3 {
  background: linear-gradient(90deg, #f56c6c 0%, #f78989 100%);
}

.timeline-value {
  width: 50px;
  text-align: right;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
</style>
