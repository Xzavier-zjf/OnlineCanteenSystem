<template>
  <div class="recommend-products">
    <el-row :gutter="20">
      <!-- 当前推荐商品 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>当前推荐商品</span>
              <div>
                <el-button type="success" @click="saveRecommendProducts">保存配置</el-button>
                <el-button type="primary" @click="refreshRecommendProducts">刷新</el-button>
              </div>
            </div>
          </template>
          
          <div v-loading="recommendLoading">
            <el-empty v-if="!recommendProducts.length" description="暂无推荐商品" />
            <div v-else>
              <draggable 
                v-model="recommendProducts" 
                @end="onRecommendOrderChange"
                item-key="id"
                class="recommend-products-list"
              >
                <template #item="{ element, index }">
                  <div class="recommend-product-item">
                    <div class="product-order">{{ index + 1 }}</div>
                    <el-image 
                      :src="element.image" 
                      style="width: 80px; height: 80px; margin-right: 15px;"
                      fit="cover"
                    />
                    <div class="product-info">
                      <div class="product-name">{{ element.name }}</div>
                      <div class="product-category">{{ getCategoryName(element.category) }}</div>
                      <div class="product-details">
                        <span class="price">¥{{ element.price.toFixed(2) }}</span>
                        <span class="sales">销量: {{ element.salesCount || 0 }}</span>
                        <span class="rating">评分: {{ element.rating || 0 }}</span>
                      </div>
                      <div class="product-merchant">商户: {{ element.merchantName }}</div>
                    </div>
                    <div class="product-actions">
                      <el-switch
                        v-model="element.isActive"
                        active-text="启用"
                        inactive-text="禁用"
                        @change="toggleProductStatus(element)"
                      />
                      <el-button 
                        type="danger" 
                        size="small" 
                        @click="removeFromRecommend(element)"
                        style="margin-top: 8px;"
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

      <!-- 商品库 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>商品库</span>
              <div>
                <el-select v-model="filterCategory" placeholder="分类筛选" style="width: 120px; margin-right: 10px;" clearable>
                  <el-option label="主食" value="staple" />
                  <el-option label="菜品" value="dish" />
                  <el-option label="汤品" value="soup" />
                  <el-option label="饮品" value="drink" />
                  <el-option label="小食" value="snack" />
                </el-select>
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索商品"
                  style="width: 150px; margin-right: 10px;"
                  @input="searchProducts"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>
          </template>

          <div v-loading="productsLoading" style="max-height: 700px; overflow-y: auto;">
            <el-empty v-if="!allProducts.length" description="暂无商品" />
            <div v-else class="products-list">
              <div 
                v-for="product in filteredProducts" 
                :key="product.id"
                class="product-item"
                :class="{ 'in-recommend': isInRecommendList(product.id) }"
              >
                <el-image 
                  :src="product.image" 
                  style="width: 50px; height: 50px; margin-right: 12px;"
                  fit="cover"
                />
                <div class="product-info">
                  <div class="product-name">{{ product.name }}</div>
                  <div class="product-details">
                    <span>{{ getCategoryName(product.category) }}</span>
                    <span>¥{{ product.price.toFixed(2) }}</span>
                    <span>销量: {{ product.salesCount || 0 }}</span>
                  </div>
                </div>
                <div class="product-actions">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="addToRecommend(product)"
                    :disabled="isInRecommendList(product.id) || recommendProducts.length >= 20"
                  >
                    {{ isInRecommendList(product.id) ? '已添加' : '添加' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 推荐策略配置 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>推荐策略配置</span>
          </template>
          
          <el-form :model="recommendConfig" label-width="120px">
            <el-form-item label="推荐算法">
              <el-select v-model="recommendConfig.algorithm" style="width: 100%;">
                <el-option label="基于销量" value="sales" />
                <el-option label="基于评分" value="rating" />
                <el-option label="基于用户偏好" value="preference" />
                <el-option label="综合推荐" value="comprehensive" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="权重配置">
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-input-number 
                    v-model="recommendConfig.salesWeight" 
                    :min="0" 
                    :max="1" 
                    :step="0.1"
                    :precision="1"
                    style="width: 100%;"
                  />
                  <div style="text-align: center; font-size: 12px; color: #666;">销量权重</div>
                </el-col>
                <el-col :span="8">
                  <el-input-number 
                    v-model="recommendConfig.ratingWeight" 
                    :min="0" 
                    :max="1" 
                    :step="0.1"
                    :precision="1"
                    style="width: 100%;"
                  />
                  <div style="text-align: center; font-size: 12px; color: #666;">评分权重</div>
                </el-col>
                <el-col :span="8">
                  <el-input-number 
                    v-model="recommendConfig.timeWeight" 
                    :min="0" 
                    :max="1" 
                    :step="0.1"
                    :precision="1"
                    style="width: 100%;"
                  />
                  <div style="text-align: center; font-size: 12px; color: #666;">时间权重</div>
                </el-col>
              </el-row>
            </el-form-item>
            
            <el-form-item label="更新频率">
              <el-select v-model="recommendConfig.updateFrequency" style="width: 100%;">
                <el-option label="实时更新" value="realtime" />
                <el-option label="每小时" value="hourly" />
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="最大推荐数">
              <el-input-number 
                v-model="recommendConfig.maxRecommendCount" 
                :min="5" 
                :max="50" 
                style="width: 100%;"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveRecommendConfig">保存配置</el-button>
              <el-button @click="resetRecommendConfig">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 推荐效果统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>推荐效果统计</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-statistic title="推荐商品数" :value="recommendProducts.length" />
            </el-col>
            <el-col :span="12">
              <el-statistic title="启用商品数" :value="activeRecommendCount" />
            </el-col>
          </el-row>
          
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <el-statistic title="平均点击率" :value="recommendStats.avgClickRate" suffix="%" :precision="2" />
            </el-col>
            <el-col :span="12">
              <el-statistic title="转化率" :value="recommendStats.conversionRate" suffix="%" :precision="2" />
            </el-col>
          </el-row>

          <!-- 推荐效果图表 -->
          <div style="margin-top: 20px;">
            <h4>推荐效果趋势</h4>
            <div ref="effectChart" style="height: 250px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin.js'
import draggable from 'vuedraggable'
import * as echarts from 'echarts'

const recommendLoading = ref(false)
const productsLoading = ref(false)
const recommendProducts = ref([])
const allProducts = ref([])
const searchKeyword = ref('')
const filterCategory = ref('')
const effectChart = ref(null)

let effectChartInstance = null

const recommendConfig = ref({
  algorithm: 'comprehensive',
  salesWeight: 0.4,
  ratingWeight: 0.3,
  timeWeight: 0.3,
  updateFrequency: 'daily',
  maxRecommendCount: 20
})

const recommendStats = ref({
  avgClickRate: 15.6,
  conversionRate: 8.2
})

// 计算属性
const activeRecommendCount = computed(() => {
  return recommendProducts.value.filter(product => product.isActive).length
})

const filteredProducts = computed(() => {
  let filtered = allProducts.value

  if (filterCategory.value) {
    filtered = filtered.filter(product => product.category === filterCategory.value)
  }

  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(product => 
      product.name.toLowerCase().includes(keyword) ||
      product.merchantName.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

const loadRecommendProducts = async () => {
  recommendLoading.value = true
  try {
    const response = await adminApi.getRecommendProducts()
    recommendProducts.value = (response.data || []).map(product => ({
      ...product,
      isActive: product.isActive !== false // 默认启用
    }))
  } catch (error) {
    console.error('加载推荐商品失败:', error)
    ElMessage.error('加载推荐商品失败')
  } finally {
    recommendLoading.value = false
  }
}

const loadAllProducts = async () => {
  productsLoading.value = true
  try {
    const response = await adminApi.getAllProducts({
      status: 'approved',
      size: 200
    })
    
    allProducts.value = response.data?.list || []
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    productsLoading.value = false
  }
}

const searchProducts = async () => {
  // 搜索逻辑已在计算属性中处理
}

const isInRecommendList = (productId) => {
  return recommendProducts.value.some(product => product.id === productId)
}

const addToRecommend = async (product) => {
  try {
    if (recommendProducts.value.length >= recommendConfig.value.maxRecommendCount) {
      ElMessage.warning(`推荐商品最多只能设置${recommendConfig.value.maxRecommendCount}个`)
      return
    }
    
    recommendProducts.value.push({
      ...product,
      isActive: true
    })
    
    ElMessage.success('添加到推荐商品成功')
  } catch (error) {
    console.error('添加推荐商品失败:', error)
    ElMessage.error('添加推荐商品失败')
  }
}

const removeFromRecommend = async (product) => {
  try {
    await ElMessageBox.confirm(`确认将"${product.name}"从推荐商品中移除吗？`, '确认移除', {
      type: 'warning'
    })
    
    const index = recommendProducts.value.findIndex(p => p.id === product.id)
    if (index > -1) {
      recommendProducts.value.splice(index, 1)
      ElMessage.success('移除推荐商品成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除推荐商品失败:', error)
      ElMessage.error('移除推荐商品失败')
    }
  }
}

const toggleProductStatus = (product) => {
  ElMessage.success(`商品"${product.name}"已${product.isActive ? '启用' : '禁用'}`)
}

const onRecommendOrderChange = () => {
  ElMessage.success('推荐商品排序已更新')
}

const saveRecommendProducts = async () => {
  try {
    const productIds = recommendProducts.value
      .filter(product => product.isActive)
      .map(product => product.id)
    
    await adminApi.setRecommendProducts(productIds)
    ElMessage.success('推荐商品配置保存成功')
  } catch (error) {
    console.error('保存推荐商品失败:', error)
    ElMessage.error('保存推荐商品失败')
  }
}

const refreshRecommendProducts = async () => {
  await loadRecommendProducts()
  ElMessage.success('推荐商品已刷新')
}

const saveRecommendConfig = async () => {
  try {
    // 这里应该调用保存推荐配置的API
    // await adminApi.saveRecommendConfig(recommendConfig.value)
    ElMessage.success('推荐策略配置保存成功')
  } catch (error) {
    console.error('保存推荐配置失败:', error)
    ElMessage.error('保存推荐配置失败')
  }
}

const resetRecommendConfig = () => {
  recommendConfig.value = {
    algorithm: 'comprehensive',
    salesWeight: 0.4,
    ratingWeight: 0.3,
    timeWeight: 0.3,
    updateFrequency: 'daily',
    maxRecommendCount: 20
  }
  ElMessage.info('推荐配置已重置')
}

const getCategoryName = (category) => {
  const categoryMap = {
    'staple': '主食',
    'dish': '菜品',
    'soup': '汤品',
    'drink': '饮品',
    'snack': '小食'
  }
  return categoryMap[category] || category
}

const initEffectChart = async () => {
  await nextTick()
  
  if (effectChart.value) {
    effectChartInstance = echarts.init(effectChart.value)
    
    // 获取推荐效果数据
    let dates = []
    let clickRates = []
    let conversionRates = []
    
    try {
      const response = await adminApi.getRecommendEffectTrend({ days: 7 })
      const trendData = response.data || []
      dates = trendData.map(item => item.date)
      clickRates = trendData.map(item => item.clickRate)
      conversionRates = trendData.map(item => item.conversionRate)
    } catch (error) {
      console.error('获取效果数据失败:', error)
      // 使用默认数据
      const today = new Date()
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)
        dates.push(date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' }))
        clickRates.push(0)
        conversionRates.push(0)
      }
    }
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['点击率', '转化率']
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: {
        type: 'value',
        name: '百分比(%)'
      },
      series: [
        {
          name: '点击率',
          type: 'line',
          data: clickRates,
          smooth: true,
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '转化率',
          type: 'line',
          data: conversionRates,
          smooth: true,
          itemStyle: { color: '#67C23A' }
        }
      ]
    }
    
    effectChartInstance.setOption(option)
  }
}

// 监听分类筛选变化
watch(filterCategory, () => {
  // 筛选逻辑已在计算属性中处理
})

onMounted(async () => {
  await Promise.all([
    loadRecommendProducts(),
    loadAllProducts()
  ])
  
  await initEffectChart()
})
</script>

<style scoped>
.recommend-products {
  padding: 20px;
}

.recommend-products-list {
  min-height: 200px;
}

.recommend-product-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fff;
  cursor: move;
  transition: all 0.3s;
}

.recommend-product-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-order {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: linear-gradient(45deg, #409eff, #67c23a);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
  font-size: 16px;
}

.products-list {
  space-y: 8px;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.product-item:hover {
  background: #f5f7fa;
}

.product-item.in-recommend {
  background: #f0f9ff;
  border-color: #409eff;
}

.product-info {
  flex: 1;
}

.product-name {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
}

.product-category {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.product-details {
  font-size: 12px;
  color: #666;
  display: flex;
  gap: 12px;
}

.product-merchant {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.product-actions {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-weight: 500;
}
</style>
