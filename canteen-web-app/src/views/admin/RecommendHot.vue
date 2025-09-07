<template>
  <div class="recommend-hot">
    <el-row :gutter="20">
      <!-- 当前热门商品 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>当前热门商品</span>
              <el-button type="primary" @click="refreshHotProducts">刷新</el-button>
            </div>
          </template>
          
          <div v-loading="hotLoading">
            <el-empty v-if="!hotProducts.length" description="暂无热门商品" />
            <div v-else>
              <draggable 
                v-model="hotProducts" 
                @end="updateHotOrder"
                item-key="id"
                class="hot-products-list"
              >
                <template #item="{ element, index }">
                  <div class="hot-product-item">
                    <div class="product-rank">{{ index + 1 }}</div>
                    <el-image 
                      :src="element.image" 
                      style="width: 60px; height: 60px; margin-right: 12px;"
                      fit="cover"
                    />
                    <div class="product-info">
                      <div class="product-name">{{ element.name }}</div>
                      <div class="product-details">
                        <span>销量: {{ element.salesCount }}</span>
                        <span>评分: {{ element.rating }}</span>
                        <span>¥{{ element.price.toFixed(2) }}</span>
                      </div>
                    </div>
                    <div class="product-actions">
                      <el-button 
                        type="danger" 
                        size="small" 
                        @click="removeFromHot(element)"
                      >
                        移除
                      </el-button>
                    </div>
                  </div>
                </template>
              </draggable>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 候选商品 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>候选商品</span>
              <div>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索商品"
                  style="width: 200px; margin-right: 10px;"
                  @input="searchProducts"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-button @click="loadCandidateProducts">刷新</el-button>
              </div>
            </div>
          </template>

          <div v-loading="candidateLoading" style="max-height: 600px; overflow-y: auto;">
            <el-empty v-if="!candidateProducts.length" description="暂无候选商品" />
            <div v-else class="candidate-products-list">
              <div 
                v-for="product in candidateProducts" 
                :key="product.id"
                class="candidate-product-item"
              >
                <el-image 
                  :src="product.image" 
                  style="width: 50px; height: 50px; margin-right: 12px;"
                  fit="cover"
                />
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-details">
                    <span>销量: {{ product.salesCount }}</span>
                    <span>评分: {{ product.rating }}</span>
                    <span>¥{{ product.price.toFixed(2) }}</span>
                  </div>
                </div>
                <div class="product-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="addToHot(product)"
                    :disabled="isInHotList(product.id)"
                  >
                    {{ isInHotList(product.id) ? '已添加' : '添加' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门商品统计 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>热门商品统计</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="热门商品数量" :value="hotProducts.length" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总销量" :value="totalSales" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="平均评分" :value="averageRating" :precision="1" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总收入" :value="totalRevenue" prefix="¥" :precision="2" />
            </el-col>
          </el-row>

          <!-- 热门趋势图表 -->
          <div style="margin-top: 20px;">
            <h4>热门商品趋势</h4>
            <div ref="trendChart" style="height: 300px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin.js'
import draggable from 'vuedraggable'
import * as echarts from 'echarts'

const hotLoading = ref(false)
const candidateLoading = ref(false)
const hotProducts = ref([])
const candidateProducts = ref([])
const searchKeyword = ref('')
const trendChart = ref(null)

let trendChartInstance = null

// 计算统计数据
const totalSales = computed(() => {
  return hotProducts.value.reduce((sum, product) => sum + product.salesCount, 0)
})

const averageRating = computed(() => {
  if (!hotProducts.value.length) return 0
  const totalRating = hotProducts.value.reduce((sum, product) => sum + product.rating, 0)
  return totalRating / hotProducts.value.length
})

const totalRevenue = computed(() => {
  return hotProducts.value.reduce((sum, product) => sum + (product.salesCount * product.price), 0)
})

const loadHotProducts = async () => {
  hotLoading.value = true
  try {
    const response = await adminApi.getHotProducts()
    hotProducts.value = response.data || []
  } catch (error) {
    console.error('加载热门商品失败:', error)
    ElMessage.error('加载热门商品失败')
  } finally {
    hotLoading.value = false
  }
}

const loadCandidateProducts = async () => {
  candidateLoading.value = true
  try {
    // 获取所有商品，按销量排序
    const response = await adminApi.getAllProducts({
      status: 'approved',
      sortBy: 'salesCount',
      sortOrder: 'desc',
      size: 50
    })
    
    candidateProducts.value = response.data?.list || []
  } catch (error) {
    console.error('加载候选商品失败:', error)
    ElMessage.error('加载候选商品失败')
  } finally {
    candidateLoading.value = false
  }
}

const searchProducts = async () => {
  if (!searchKeyword.value.trim()) {
    await loadCandidateProducts()
    return
  }
  
  candidateLoading.value = true
  try {
    const response = await adminApi.getAllProducts({
      keyword: searchKeyword.value,
      status: 'approved',
      sortBy: 'salesCount',
      sortOrder: 'desc',
      size: 50
    })
    
    candidateProducts.value = response.data?.list || []
  } catch (error) {
    console.error('搜索商品失败:', error)
    ElMessage.error('搜索商品失败')
  } finally {
    candidateLoading.value = false
  }
}

const isInHotList = (productId) => {
  return hotProducts.value.some(product => product.id === productId)
}

const addToHot = async (product) => {
  try {
    if (hotProducts.value.length >= 10) {
      ElMessage.warning('热门商品最多只能设置10个')
      return
    }
    
    hotProducts.value.push(product)
    await saveHotProducts()
    ElMessage.success('添加到热门商品成功')
  } catch (error) {
    console.error('添加热门商品失败:', error)
    ElMessage.error('添加热门商品失败')
  }
}

const removeFromHot = async (product) => {
  try {
    await ElMessageBox.confirm(`确认将"${product.name}"从热门商品中移除吗？`, '确认移除', {
      type: 'warning'
    })
    
    const index = hotProducts.value.findIndex(p => p.id === product.id)
    if (index > -1) {
      hotProducts.value.splice(index, 1)
      await saveHotProducts()
      ElMessage.success('移除热门商品成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除热门商品失败:', error)
      ElMessage.error('移除热门商品失败')
    }
  }
}

const updateHotOrder = async () => {
  try {
    await saveHotProducts()
    ElMessage.success('热门商品排序已更新')
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
  }
}

const saveHotProducts = async () => {
  const productIds = hotProducts.value.map(product => product.id)
  await adminApi.setHotProducts(productIds)
}

const refreshHotProducts = async () => {
  await loadHotProducts()
  ElMessage.success('热门商品已刷新')
}

const initTrendChart = async () => {
  await nextTick()
  
  if (trendChart.value) {
    trendChartInstance = echarts.init(trendChart.value)
    
    // 获取热门商品趋势数据
    let dates = []
    let salesData = []
    
    try {
      const response = await adminApi.getHotProductsTrend({ days: 7 })
      const trendData = response.data || []
      dates = trendData.map(item => item.date)
      salesData = trendData.map(item => item.sales)
    } catch (error) {
      console.error('获取趋势数据失败:', error)
      // 使用默认数据
      const today = new Date()
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)
        dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
        salesData.push(0)
      }
    }
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value',
        name: '销量'
      },
      series: [
        {
          name: '热门商品销量',
          type: 'line',
          data: salesData,
          smooth: true,
          itemStyle: { color: '#ff6b6b' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(255, 107, 107, 0.3)' },
                { offset: 1, color: 'rgba(255, 107, 107, 0.1)' }
              ]
            }
          }
        }
      ]
    }
    
    trendChartInstance.setOption(option)
  }
}

onMounted(async () => {
  await Promise.all([
    loadHotProducts(),
    loadCandidateProducts()
  ])
  
  await initTrendChart()
})
</script>

<style scoped>
.recommend-hot {
  padding: 20px;
}

.hot-products-list {
  min-height: 200px;
}

.hot-product-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
  background: #fff;
  cursor: move;
}

.hot-product-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.product-rank {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 12px;
  font-size: 14px;
}

.candidate-products-list {
  space-y: 8px;
}

.candidate-product-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 8px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.product-details {
  font-size: 12px;
  color: #666;
  display: flex;
  gap: 12px;
}

.product-actions {
  margin-left: 12px;
}
</style>
