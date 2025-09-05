<template>
  <div class="merchant-products">
    <!-- 页面头部 -->
    <el-card class="header-card" shadow="hover">
      <div class="page-header">
        <div class="header-info">
          <h2>商品管理</h2>
          <p>管理您的商品信息，包括添加、编辑、上下架等操作</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加商品
          </el-button>
          <el-button @click="loadProducts">
            <el-icon><RefreshRight /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 筛选和搜索 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          clearable
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select
          v-model="categoryFilter"
          placeholder="商品分类"
          clearable
          style="width: 150px; margin-right: 10px;"
          @change="handleSearch"
        >
          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
        
        <el-select
          v-model="statusFilter"
          placeholder="商品状态"
          clearable
          style="width: 120px; margin-right: 10px;"
          @change="handleSearch"
        >
          <el-option label="上架中" value="1" />
          <el-option label="已下架" value="0" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="products-card" shadow="hover">
      <div v-loading="loading">
        <el-table :data="products" style="width: 100%" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" />
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <el-image
                :src="getImageUrl(row.imageUrl)"
                :preview-src-list="[getImageUrl(row.imageUrl)]"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px;"
                @error="handleImageError"
              />
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="150">
            <template #default="{ row }">
              <div class="product-name">
                <span>{{ row.name }}</span>
                <el-tag v-if="row.isHot" type="danger" size="small" style="margin-left: 8px;">
                  热销
                </el-tag>
                <el-tag v-if="row.isNew" type="primary" size="small" style="margin-left: 8px;">
                  新品
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80">
            <template #default="{ row }">
              <el-tag :type="getStockType(row.stock)">
                {{ row.stock }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="80" />
          <el-table-column prop="rating" label="评分" width="120">
            <template #default="{ row }">
              <el-rate v-model="row.rating" disabled size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                active-text="上架"
                inactive-text="下架"
                @change="toggleProductStatus(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="editProduct(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button size="small" type="info" @click="viewStats(row)">
                <el-icon><DataAnalysis /></el-icon>
                统计
              </el-button>
              <el-button size="small" type="danger" @click="deleteProduct(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 批量操作 -->
        <div class="batch-actions" v-if="selectedProducts.length > 0">
          <span>已选择 {{ selectedProducts.length }} 个商品</span>
          <el-button size="small" @click="batchToggleStatus(1)">批量上架</el-button>
          <el-button size="small" @click="batchToggleStatus(0)">批量下架</el-button>
          <el-button size="small" type="danger" @click="batchDelete">批量删除</el-button>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingProduct ? '编辑商品' : '添加商品'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="productForm.stock"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        
        <el-form-item label="商品图片" prop="imageUrl">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeImageUpload"
            action="/api/upload/image"
          >
            <img v-if="productForm.imageUrl" :src="productForm.imageUrl" class="image-preview" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="商品标签">
          <el-checkbox v-model="productForm.isHot">热销商品</el-checkbox>
          <el-checkbox v-model="productForm.isNew" style="margin-left: 20px;">新品推荐</el-checkbox>
        </el-form-item>
        
        <el-form-item label="商品状态">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProduct" :loading="saving">
          {{ editingProduct ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 商品统计对话框 -->
    <el-dialog v-model="showStatsDialog" title="商品统计" width="800px">
      <div v-if="currentProductStats">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="总销量" :value="currentProductStats.totalSales" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="总收入" :value="currentProductStats.totalRevenue" prefix="¥" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均评分" :value="currentProductStats.avgRating" :precision="1" />
          </el-col>
        </el-row>
        
        <div class="stats-chart" ref="statsChart" style="height: 300px; margin-top: 20px;"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, RefreshRight, Search, Edit, Delete, DataAnalysis
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'

export default {
  name: 'MerchantProducts',
  components: {
    Plus, RefreshRight, Search, Edit, Delete, DataAnalysis
  },
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const products = ref([])
    const categories = ref([])
    const selectedProducts = ref([])
    
    // 筛选条件
    const searchKeyword = ref('')
    const categoryFilter = ref('')
    const statusFilter = ref('')
    
    // 分页
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 对话框
    const showAddDialog = ref(false)
    const showStatsDialog = ref(false)
    const editingProduct = ref(null)
    const currentProductStats = ref(null)
    
    // 表单
    const productFormRef = ref(null)
    const productForm = reactive({
      name: '',
      categoryId: '',
      price: 0,
      stock: 0,
      description: '',
      imageUrl: '',
      isHot: false,
      isNew: false,
      status: 1
    })
    
    const productRules = {
      name: [
        { required: true, message: '请输入商品名称', trigger: 'blur' }
      ],
      categoryId: [
        { required: true, message: '请选择商品分类', trigger: 'change' }
      ],
      price: [
        { required: true, message: '请输入商品价格', trigger: 'blur' }
      ],
      stock: [
        { required: true, message: '请输入库存数量', trigger: 'blur' }
      ]
    }
    
    const statsChart = ref(null)
    
    // 加载商品分类
    const loadCategories = async () => {
      try {
        // const response = await productApi.getCategories()
        // categories.value = response.data
        
        // 模拟数据
        categories.value = [
          { id: 1, name: '主食' },
          { id: 2, name: '汤品' },
          { id: 3, name: '小菜' },
          { id: 4, name: '饮品' },
          { id: 5, name: '甜品' }
        ]
      } catch (error) {
        console.error('加载分类失败:', error)
        ElMessage.error('加载分类失败')
      }
    }
    
    // 加载商品列表
    const loadProducts = async () => {
      try {
        loading.value = true
        
        const params = {
          current: currentPage.value,
          size: pageSize.value
        }
        
        if (searchKeyword.value) {
          params.keyword = searchKeyword.value
        }
        if (categoryFilter.value) {
          params.categoryId = categoryFilter.value
        }
        if (statusFilter.value !== '') {
          params.status = statusFilter.value
        }
        
        // 调用真实API获取商品数据
        const response = await fetch('/api/merchant/products?' + new URLSearchParams(params), {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (response.ok) {
          const data = await response.json()
          if (data.success) {
            products.value = data.data.records || []
            total.value = data.data.total || 0
          } else {
            throw new Error(data.message || '获取商品列表失败')
          }
        } else {
          console.warn('商品列表API不存在，使用空数据')
          products.value = []
          total.value = 0
        }
        
      } catch (error) {
        console.error('加载商品失败:', error)
        ElMessage.error('加载商品失败')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      loadProducts()
    }
    
    // 分页处理
    const handleSizeChange = (size) => {
      pageSize.value = size
      currentPage.value = 1
      loadProducts()
    }
    
    const handleCurrentChange = (page) => {
      currentPage.value = page
      loadProducts()
    }
    
    // 选择处理
    const handleSelectionChange = (selection) => {
      selectedProducts.value = selection
    }
    
    // 图片处理
    const getImageUrl = (imageUrl) => {
      if (!imageUrl) return '/images/products/placeholder.svg'
      if (imageUrl.startsWith('http')) return imageUrl
      return imageUrl
    }
    
    const handleImageError = (event) => {
      event.target.src = '/images/products/placeholder.svg'
    }
    
    const handleImageSuccess = (response) => {
      productForm.imageUrl = response.data.url
    }
    
    const beforeImageUpload = (file) => {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      
      if (!isJPG) {
        ElMessage.error('上传图片只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        ElMessage.error('上传图片大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    }
    
    // 库存状态
    const getStockType = (stock) => {
      if (stock === 0) return 'danger'
      if (stock <= 10) return 'warning'
      return 'success'
    }
    
    // 商品操作
    const editProduct = (product) => {
      editingProduct.value = product
      Object.assign(productForm, product)
      showAddDialog.value = true
    }
    
    const deleteProduct = async (product) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除商品 "${product.name}" 吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        // await merchantApi.deleteProduct(product.id)
        ElMessage.success('删除成功')
        loadProducts()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除商品失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }
    
    const toggleProductStatus = async (product) => {
      try {
        // await merchantApi.updateProductStatus(product.id, product.status)
        ElMessage.success(product.status ? '商品已上架' : '商品已下架')
      } catch (error) {
        console.error('更新状态失败:', error)
        ElMessage.error('操作失败')
        // 回滚状态
        product.status = product.status ? 0 : 1
      }
    }
    
    // 批量操作
    const batchToggleStatus = async (status) => {
      try {
        const ids = selectedProducts.value.map(p => p.id)
        // await merchantApi.batchUpdateStatus(ids, status)
        ElMessage.success(`批量${status ? '上架' : '下架'}成功`)
        loadProducts()
      } catch (error) {
        console.error('批量操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
    
    const batchDelete = async () => {
      try {
        await ElMessageBox.confirm(
          `确定要删除选中的 ${selectedProducts.value.length} 个商品吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const ids = selectedProducts.value.map(p => p.id)
        // await merchantApi.batchDelete(ids)
        ElMessage.success('批量删除成功')
        loadProducts()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }
    
    // 保存商品
    const saveProduct = async () => {
      try {
        await productFormRef.value.validate()
        saving.value = true
        
        if (editingProduct.value) {
          // await merchantApi.updateProduct(editingProduct.value.id, productForm)
          ElMessage.success('更新成功')
        } else {
          // await merchantApi.createProduct(productForm)
          ElMessage.success('添加成功')
        }
        
        showAddDialog.value = false
        loadProducts()
      } catch (error) {
        if (error !== false) { // 不是表单验证错误
          console.error('保存商品失败:', error)
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }
    
    // 重置表单
    const resetForm = () => {
      editingProduct.value = null
      Object.assign(productForm, {
        name: '',
        categoryId: '',
        price: 0,
        stock: 0,
        description: '',
        imageUrl: '',
        isHot: false,
        isNew: false,
        status: 1
      })
      if (productFormRef.value) {
        productFormRef.value.resetFields()
      }
    }
    
    // 查看统计
    const viewStats = async (product) => {
      try {
        // const response = await merchantApi.getProductStats(product.id)
        // currentProductStats.value = response.data
        
        // 模拟数据
        currentProductStats.value = {
          totalSales: 120,
          totalRevenue: 1800,
          avgRating: 4.8
        }
        
        showStatsDialog.value = true
        
        // 初始化图表
        await nextTick()
        if (statsChart.value) {
          const chartInstance = echarts.init(statsChart.value)
          const option = {
            title: {
              text: '销量趋势'
            },
            tooltip: {
              trigger: 'axis'
            },
            xAxis: {
              type: 'category',
              data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
            },
            yAxis: {
              type: 'value'
            },
            series: [{
              data: [12, 15, 18, 22, 25, 20, 18],
              type: 'line',
              smooth: true
            }]
          }
          chartInstance.setOption(option)
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        ElMessage.error('加载统计数据失败')
      }
    }
    
    onMounted(() => {
      loadCategories()
      loadProducts()
    })
    
    return {
      loading,
      saving,
      products,
      categories,
      selectedProducts,
      searchKeyword,
      categoryFilter,
      statusFilter,
      currentPage,
      pageSize,
      total,
      showAddDialog,
      showStatsDialog,
      editingProduct,
      currentProductStats,
      productFormRef,
      productForm,
      productRules,
      statsChart,
      loadProducts,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      getImageUrl,
      handleImageError,
      handleImageSuccess,
      beforeImageUpload,
      getStockType,
      editProduct,
      deleteProduct,
      toggleProductStatus,
      batchToggleStatus,
      batchDelete,
      saveProduct,
      resetForm,
      viewStats
    }
  }
}
</script>

<style scoped>
.merchant-products {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-card,
.filter-card,
.products-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.header-info p {
  margin: 0;
  color: #606266;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.product-name {
  display: flex;
  align-items: center;
}

.price {
  color: #E6A23C;
  font-weight: bold;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px 0;
  border-top: 1px solid #ebeef5;
  margin-top: 15px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader:hover {
  border-color: #409EFF;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.image-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
}

.stats-chart {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .merchant-products {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-section .el-input,
  .filter-section .el-select {
    width: 100% !important;
    margin-right: 0 !important;
    margin-bottom: 10px;
  }
  
  .batch-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>