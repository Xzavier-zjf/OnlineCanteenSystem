<template>
  <div class="products">
    <!-- 分类导航 -->
    <el-card class="category-card" shadow="hover">
      <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :name="category.id.toString()"
        ></el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 推荐商品 -->
    <el-card class="recommend-card" shadow="hover" v-if="recommendProducts.length > 0">
      <template #header>
        <div class="section-header">
          <el-icon><Star /></el-icon>
          <span>为您推荐</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="product in recommendProducts" :key="'rec-' + product.id">
          <div class="recommend-item">
            <img :src="getImageUrl(product.imageUrl)" @error="handleImageError" class="recommend-image" />
            <div class="recommend-info">
              <h4>{{ product.name }}</h4>
              <p class="recommend-reason">{{ product.reason }}</p>
              <div class="recommend-price">¥{{ product.price }}</div>
              <el-button type="primary" size="small" @click="addToCart(product)">
                加入购物车
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="products-card" shadow="hover">
      <template #header>
        <div class="products-header">
          <span>商品列表</span>
          <div class="search-filters">
            <el-input 
              v-model="keyword" 
              placeholder="搜索商品名称" 
              clearable 
              @keyup.enter="handleSearch" 
              @clear="handleSearch"
              style="width: 200px; margin-right: 10px;" 
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select 
              v-model="priceRange" 
              placeholder="价格区间" 
              clearable 
              @change="handleSearch"
              style="width: 140px; margin-right: 10px;"
            >
              <el-option label="0-5元" value="0-5" />
              <el-option label="5-10元" value="5-10" />
              <el-option label="10-15元" value="10-15" />
              <el-option label="15-20元" value="15-20" />
              <el-option label="20-30元" value="20-30" />
              <el-option label="30-50元" value="30-50" />
              <el-option label="50元以上" value="50+" />
            </el-select>
            <el-select 
              v-model="sortBy" 
              placeholder="排序方式" 
              @change="handleSearch"
              style="width: 140px; margin-right: 10px;"
            >
              <el-option label="默认排序" value="default" />
              <el-option label="价格升序" value="price_asc" />
              <el-option label="价格降序" value="price_desc" />
              <el-option label="销量优先" value="sales_desc" />
              <el-option label="评分优先" value="rating_desc" />
              <el-option label="最新上架" value="create_time_desc" />
            </el-select>
            <el-select 
              v-model="stockFilter" 
              placeholder="库存状态" 
              clearable 
              @change="handleSearch"
              style="width: 120px; margin-right: 10px;"
            >
              <el-option label="有库存" value="in_stock" />
              <el-option label="库存不足" value="low_stock" />
              <el-option label="缺货" value="out_of_stock" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
          <div class="cart-info" v-if="cartItems.length > 0">
            <el-badge :value="cartItems.length" class="cart-badge">
              <el-button type="primary" @click="showCart = true">
                <el-icon><ShoppingCart /></el-icon>
                购物车
              </el-button>
            </el-badge>
          </div>
        </div>
      </template>
      
      <div v-loading="loading">
        <div v-if="products.length === 0 && !loading" class="empty-products">
          <el-empty description="暂无商品" :image-size="120">
            <template #description>
              <p>没有找到符合条件的商品</p>
              <p style="color: #909399; font-size: 14px;">试试调整搜索条件或浏览其他分类</p>
            </template>
          </el-empty>
        </div>
        
        <el-row :gutter="20" v-else>
          <el-col :span="6" v-for="product in products" :key="product.id">
            <el-card class="product-card" shadow="hover">
              <div class="product-image-container">
                <img :src="getImageUrl(product.imageUrl)" @error="handleImageError" class="product-image" />
                <div class="product-badges">
                  <el-tag v-if="product.sales > 100" type="success" size="small">热销</el-tag>
                  <el-tag v-if="product.rating >= 4.5" type="warning" size="small">高评分</el-tag>
                  <el-tag v-if="product.isNew" type="primary" size="small">新品</el-tag>
                </div>
                <div class="stock-status" v-if="product.stock !== undefined">
                  <el-tag 
                    :type="getStockStatusType(product.stock)" 
                    size="small"
                    class="stock-tag"
                  >
                    {{ getStockStatusText(product.stock) }}
                  </el-tag>
                </div>
              </div>
              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-stats">
                  <div class="stats-row">
                    <span class="product-sales">已售 {{ product.sales || 0 }}</span>
                    <span class="product-stock" v-if="product.stock !== undefined">
                      库存: {{ product.stock }}
                    </span>
                  </div>
                  <div class="rating-row">
                    <el-rate v-model="product.rating" disabled show-score size="small" />
                  </div>
                </div>
                <div class="product-footer">
                  <div class="price-info">
                    <span class="product-price">¥{{ product.price }}</span>
                    <span v-if="product.originalPrice && product.originalPrice > product.price" 
                          class="original-price">
                      ¥{{ product.originalPrice }}
                    </span>
                  </div>
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="addToCart(product)"
                    :disabled="product.stock === 0"
                  >
                    <el-icon><Plus /></el-icon>
                    {{ product.stock === 0 ? '缺货' : '加入购物车' }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[6, 12, 18, 24]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 购物车抽屉 -->
    <el-drawer v-model="showCart" title="购物车" size="400px">
      <div class="cart-content">
        <div v-if="cartItems.length === 0" class="empty-cart">
          <el-empty description="购物车为空" />
        </div>
        <div v-else>
          <div class="cart-item" v-for="item in cartItems" :key="item.id">
            <img :src="getImageUrl(item.imageUrl)" @error="handleImageError" class="cart-item-image" />
            <div class="cart-item-info">
              <h4>{{ item.name }}</h4>
            <div class="cart-item-controls">
              <div class="quantity-controls">
                <el-button 
                  size="small" 
                  @click="decreaseQuantity(item)"
                  :disabled="item.quantity <= 1"
                >
                  <el-icon><Minus /></el-icon>
                </el-button>
                <span class="quantity-display">{{ item.quantity }}</span>
                <el-button 
                  size="small" 
                  @click="increaseQuantity(item)"
                  :disabled="item.quantity >= 99"
                >
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>
              <span class="cart-item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              <el-button type="danger" size="small" @click="removeFromCart(item.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            </div>
          </div>
          
          <el-divider />
          
          <div class="cart-summary">
            <div class="total-price">
              总计: ¥{{ totalPrice.toFixed(2) }}
            </div>
            <el-button size="large" @click="clearCart" :disabled="cartItems.length === 0">
              清空购物车
            </el-button>
            <el-button type="primary" size="large" @click="checkout" :disabled="cartItems.length === 0 || creatingOrder" :loading="creatingOrder">
              立即下单
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productApi, recommendApi, orderApi } from '../api'
import { Star, ShoppingCart, Plus, Delete, Search, Minus } from '@element-plus/icons-vue'

export default {
  name: 'Products',
  components: {
    Star, ShoppingCart, Plus, Delete, Search, Minus
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const categories = ref([])
    const products = ref([])
    const recommendProducts = ref([])
    const keyword = ref('')
    const priceRange = ref('')
    const sortBy = ref('default')
    const stockFilter = ref('')
    const activeCategory = ref('all')
    const currentPage = ref(1)
    const pageSize = ref(6)
    const total = ref(0)
    const showCart = ref(false)
    const cartItems = ref([])
    const creatingOrder = ref(false)
    
    const getUserInfo = () => {
      const userInfoStr = localStorage.getItem('userInfo')
      const token = localStorage.getItem('token')
      
      console.log('获取用户信息 - userInfoStr:', userInfoStr)
      console.log('获取用户信息 - token:', token)
      
      if (!token || token === 'null' || token === 'undefined') {
        console.log('Token无效，返回空对象')
        return {}
      }
      
      if (userInfoStr && userInfoStr !== 'undefined' && userInfoStr !== 'null') {
        try {
          const parsed = JSON.parse(userInfoStr)
          if (parsed && typeof parsed === 'object' && parsed.id) {
            console.log('用户信息解析成功:', parsed)
            return parsed
          } else {
            console.warn('用户信息格式无效:', parsed)
            return {}
          }
        } catch (e) {
          console.error('解析用户信息失败:', e)
          return {}
        }
      }
      console.log('用户信息不存在，返回空对象')
      return {}
    }
    
    const userInfo = getUserInfo()
    
    const totalPrice = computed(() => {
      return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
    })
    
    const loadCategories = async () => {
      try {
        const response = await productApi.getCategories()
        categories.value = response.data
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    }
    
    const loadProducts = async () => {
      try {
        loading.value = true
        const params = {
          current: currentPage.value,
          size: pageSize.value
        }
        
        // 分类筛选 - 确保正确处理分类ID
        if (activeCategory.value && activeCategory.value !== 'all') {
          params.categoryId = activeCategory.value
          console.log('设置分类筛选参数:', params.categoryId)
        }
        
        if (keyword.value && keyword.value.trim()) {
          params.keyword = keyword.value.trim()
        }
        if (priceRange.value) {
          params.priceRange = priceRange.value
        }
        if (sortBy.value && sortBy.value !== 'default') {
          params.sortBy = sortBy.value
        }
        if (stockFilter.value) {
          params.stockFilter = stockFilter.value
        }
        
        console.log('商品加载参数:', params)
        const response = await productApi.getProducts(params)
        console.log('商品加载响应:', response)
        
        if (response && response.data) {
          products.value = response.data.records || []
          total.value = response.data.total || 0
          console.log(`加载了 ${products.value.length} 个商品，总数: ${total.value}`)
        } else {
          products.value = []
          total.value = 0
        }
      } catch (error) {
        console.error('加载商品失败:', error)
        products.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }
    
    const loadRecommendProducts = async () => {
      try {
        let response
        
        // 首先尝试获取个性化推荐
        if (userInfo.id) {
          try {
            response = await recommendApi.getRecommendProducts(userInfo.id, 3)
            if (response && response.data && Array.isArray(response.data) && response.data.length > 0) {
              // 为推荐商品添加推荐理由
              recommendProducts.value = response.data.map(product => ({
                ...product,
                reason: product.reason || getRecommendReason(product)
              }))
              console.log('个性化推荐加载成功:', recommendProducts.value)
              return
            }
          } catch (personalizedError) {
            console.warn('个性化推荐失败，尝试热门推荐:', personalizedError)
            // 如果是401错误，说明需要登录，跳过个性化推荐
            if (personalizedError.response?.status === 401) {
              console.log('推荐服务需要登录，使用匿名推荐')
            }
          }
        }
        
        // 如果个性化推荐失败或无数据，使用热门推荐
        try {
          response = await recommendApi.getHotRecommendations(3)
          if (response && response.data && Array.isArray(response.data)) {
            recommendProducts.value = response.data.map(product => ({
              ...product,
              reason: product.reason || '热门商品，好评如潮'
            }))
            console.log('热门推荐加载成功:', recommendProducts.value)
          } else {
            // 如果热门推荐也没有数据，尝试获取高评分商品作为推荐
            const productsResponse = await productApi.getProducts({ 
              current: 1, 
              size: 3, 
              sortBy: 'rating_desc' 
            })
            if (productsResponse && productsResponse.data && productsResponse.data.records) {
              recommendProducts.value = productsResponse.data.records.slice(0, 3).map(product => ({
                ...product,
                reason: '高评分商品，品质保证'
              }))
              console.log('使用高评分商品作为推荐:', recommendProducts.value)
            } else {
              recommendProducts.value = []
            }
          }
        } catch (hotError) {
          console.error('热门推荐失败，尝试获取普通商品:', hotError)
          // 最后的备选方案：获取任意商品作为推荐
          try {
            const fallbackResponse = await productApi.getProducts({ current: 1, size: 3 })
            if (fallbackResponse && fallbackResponse.data && fallbackResponse.data.records) {
              recommendProducts.value = fallbackResponse.data.records.slice(0, 3).map(product => ({
                ...product,
                reason: '精选商品，值得尝试'
              }))
              console.log('使用普通商品作为推荐:', recommendProducts.value)
            } else {
              recommendProducts.value = []
            }
          } catch (fallbackError) {
            console.error('所有推荐方案都失败了:', fallbackError)
            recommendProducts.value = []
          }
        }
        
      } catch (error) {
        console.error('加载推荐商品完全失败:', error)
        recommendProducts.value = []
      }
    }
    
    // 生成推荐理由的辅助函数
    const getRecommendReason = (product) => {
      if (product.sales > 100) {
        return '热销商品，深受喜爱'
      } else if (product.rating >= 4.5) {
        return '高评分商品，品质优秀'
      } else if (product.isHot) {
        return '热门推荐，不容错过'
      } else {
        return '为您精选，值得品尝'
      }
    }
    
    const handleCategoryChange = (categoryId) => {
      console.log('分类切换:', categoryId)
      activeCategory.value = categoryId
      currentPage.value = 1
      loadProducts()
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadProducts()
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
      loadProducts()
    }
    
    const handleSearch = () => {
      currentPage.value = 1
      console.log('搜索参数:', {
        keyword: keyword.value,
        priceRange: priceRange.value,
        sortBy: sortBy.value,
        stockFilter: stockFilter.value,
        activeCategory: activeCategory.value
      })
      loadProducts()
    }
    
    const addToCart = (product) => {
      const existingItem = cartItems.value.find(item => item.id === product.id)
      if (existingItem) {
        existingItem.quantity += 1
      } else {
        cartItems.value.push({
          ...product,
          quantity: 1
        })
      }
      ElMessage.success('已添加到购物车')
      
      // 记录用户行为
      if (userInfo.id) {
        recommendApi.recordBehavior({
          userId: userInfo.id,
          productId: product.id,
          action: 'add_to_cart',
          timestamp: new Date().toISOString()
        }).catch(console.error)
      }
    }
    
    const increaseQuantity = (item) => {
      if (item.quantity < 99) {
        item.quantity += 1
        ElMessage.success(`${item.name} 数量已增加`)
      }
    }
    
    const decreaseQuantity = (item) => {
      if (item.quantity > 1) {
        item.quantity -= 1
        ElMessage.success(`${item.name} 数量已减少`)
      }
    }
    
    const updateCartItem = (item) => {
      // 数量已通过 v-model 更新
    }
    
    const removeFromCart = (productId) => {
      const index = cartItems.value.findIndex(item => item.id === productId)
      if (index > -1) {
        cartItems.value.splice(index, 1)
        ElMessage.success('已从购物车移除')
      }
    }
    
    const clearCart = async () => {
      if (cartItems.value.length > 0) {
        try {
          await ElMessageBox.confirm('确定要清空购物车吗？', '确认清空', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          cartItems.value = []
          ElMessage.success('购物车已清空')
        } catch (error) {
          // 用户取消操作
        }
      }
    }
    
    const checkout = async () => {
      // 直接从localStorage获取用户信息，简化验证逻辑
      const token = localStorage.getItem('token')
      const userInfoStr = localStorage.getItem('userInfo')
      
      console.log('开始下单流程')
      console.log('Token:', token)
      console.log('UserInfo字符串:', userInfoStr)
      
      // 简化验证条件：只要有token和userInfo就认为已登录
      if (!token || !userInfoStr || token === 'null' || userInfoStr === 'null') {
        console.log('验证失败：缺少token或用户信息')
        ElMessage.warning('请先登录后再下单')
        router.push('/login')
        return
      }
      
      let currentUserInfo
      try {
        currentUserInfo = JSON.parse(userInfoStr)
        console.log('解析后的用户信息:', currentUserInfo)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        ElMessage.warning('用户信息异常，请重新登录')
        router.push('/login')
        return
      }
      
      // 如果解析成功但没有用户ID，使用默认值
      if (!currentUserInfo.id) {
        currentUserInfo.id = currentUserInfo.userId || Date.now()
        console.log('使用默认用户ID:', currentUserInfo.id)
      }
      
      if (cartItems.value.length === 0) {
        ElMessage.warning('购物车为空，请先添加商品')
        return
      }
      
      try {
        await ElMessageBox.confirm(
          `确认下单？共 ${cartItems.value.length} 件商品，总金额 ¥${totalPrice.value.toFixed(2)}`,
          '确认下单',
          {
            confirmButtonText: '确认下单',
            cancelButtonText: '取消',
            type: 'info'
          }
        )
        
        creatingOrder.value = true
        
        // 显示下单进度
        const loadingMessage = ElMessage({
          message: '正在创建订单...',
          type: 'info',
          duration: 0
        })
        
        const orderItems = cartItems.value.map(item => ({
          productId: item.id,
          productName: item.name,
          price: item.price,
          quantity: item.quantity
        }))
        
        const orderData = {
          userId: currentUserInfo.id,
          items: orderItems,
          totalAmount: totalPrice.value,
          remark: ''
        }
        
        console.log('订单数据:', orderData)
        
        const response = await orderApi.createOrder(orderData)
        console.log('订单创建响应:', response)
        
        loadingMessage.close()
        
        // 检查响应数据
        if (response && response.code === 200 && response.data) {
          // 下单成功提示
          await ElMessageBox.alert(
            `订单创建成功！\n订单号：${response.data.orderNo || '未知'}\n金额：¥${totalPrice.value.toFixed(2)}\n请及时支付`,
            '下单成功',
            {
              confirmButtonText: '查看订单',
              type: 'success'
            }
          )
        } else {
          throw new Error(response?.message || '订单创建失败')
        }
        
        cartItems.value = []
        showCart.value = false
        
        // 跳转到订单页面
        router.push('/orders')
        
      } catch (error) {
        if (error !== 'cancel') {
          console.error('创建订单失败:', error)
          ElMessage.error(`创建订单失败: ${error.message || '请重试'}`)
        }
      } finally {
        creatingOrder.value = false
      }
    }
    
    // 图片处理方法
    const getImageUrl = (imageUrl) => {
      if (!imageUrl) {
        return '/images/products/placeholder.svg'
      }
      // 如果是相对路径，添加前缀
      if (imageUrl.startsWith('/images/')) {
        return imageUrl
      }
      // 如果是完整URL，直接返回
      if (imageUrl.startsWith('http')) {
        return imageUrl
      }
      // 其他情况，返回占位图
      return '/images/products/placeholder.svg'
    }
    
    const handleImageError = (event) => {
      event.target.src = '/images/products/placeholder.svg'
    }
    
    const getStockStatusType = (stock) => {
      if (stock === 0) return 'danger'
      if (stock <= 10) return 'warning'
      return 'success'
    }
    
    const getStockStatusText = (stock) => {
      if (stock === 0) return '缺货'
      if (stock <= 10) return '库存不足'
      return '有库存'
    }

    onMounted(() => {
      loadCategories()
      loadProducts()
      loadRecommendProducts()
    })
    
    return {
      loading,
      categories,
      products,
      recommendProducts,
      keyword,
      priceRange,
      sortBy,
      stockFilter,
      activeCategory,
      currentPage,
      pageSize,
      total,
      showCart,
      cartItems,
      totalPrice,
      handleCategoryChange,
      handleSizeChange,
      handleCurrentChange,
      handleSearch,
      addToCart,
      increaseQuantity,
      decreaseQuantity,
      updateCartItem,
      removeFromCart,
      clearCart,
      checkout,
      creatingOrder,
      getImageUrl,
      handleImageError,
      getStockStatusType,
      getStockStatusText
    }
  }
}
</script>

<style scoped>
.products {
  max-width: 1200px;
  margin: 0 auto;
}

.category-card, .recommend-card, .products-card {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  font-size: 1.1em;
  font-weight: bold;
}

.section-header .el-icon {
  margin-right: 8px;
}

.products-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.search-filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.cart-badge {
  margin-left: 10px;
}

.recommend-item {
  display: flex;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
}

.recommend-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.recommend-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.recommend-reason {
  color: #909399;
  font-size: 0.9em;
  margin: 0 0 8px 0;
}

.recommend-price {
  color: #E6A23C;
  font-weight: bold;
  margin-bottom: 8px;
}

.empty-products {
  text-align: center;
  padding: 60px 20px;
}

.product-card {
  margin-bottom: 20px;
  height: 380px;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-2px);
}

.product-image-container {
  position: relative;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 8px 0;
  font-size: 1.1em;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  color: #909399;
  font-size: 0.9em;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-size: 0.8em;
}

.product-sales {
  color: #909399;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.product-price {
  color: #E6A23C;
  font-size: 1.2em;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.cart-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.empty-cart {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-item {
  display: flex;
  padding: 15px 0;
  border-bottom: 1px solid #ebeef5;
}

.cart-item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.cart-item-info {
  flex: 1;
}

.cart-item-info h4 {
  margin: 0 0 10px 0;
  color: #303133;
}

.cart-item-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 5px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 2px;
}

.quantity-display {
  min-width: 30px;
  text-align: center;
  font-weight: bold;
  color: #303133;
}

/* 商品卡片增强样式 */
.product-badges {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  z-index: 2;
}

.stock-status {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
}

.stock-tag {
  font-size: 12px;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-stock {
  color: #909399;
  font-size: 0.85em;
}

.rating-row {
  display: flex;
  justify-content: center;
}

.price-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.original-price {
  color: #909399;
  font-size: 0.8em;
  text-decoration: line-through;
  margin-top: 2px;
}

/* 搜索筛选区域样式 */
.search-filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 768px) {
  .search-filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-filters .el-input,
  .search-filters .el-select {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 8px;
  }
}

.cart-item-price {
  color: #E6A23C;
  font-weight: bold;
  min-width: 60px;
}

.cart-summary {
  margin-top: auto;
  padding: 20px 0;
  text-align: center;
}

.total-price {
  font-size: 1.2em;
  font-weight: bold;
  color: #E6A23C;
  margin-bottom: 15px;
}
</style>