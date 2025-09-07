<template>
  <div class="admin-products">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="searchForm" inline>
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option 
                v-for="category in categories" 
                :key="category.id" 
                :label="category.name" 
                :value="category.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="商户">
            <el-input v-model="searchForm.merchantName" placeholder="请输入商户名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="上架" value="ACTIVE" />
              <el-option label="下架" value="INACTIVE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchProducts">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 商品列表 -->
      <el-table :data="Array.isArray(productList) ? productList : []" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image 
              :src="scope.row.imageUrl" 
              :preview-src-list="[scope.row.imageUrl]"
              style="width: 60px; height: 60px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" width="200" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="merchantName" label="商户" width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewProduct(scope.row)">
              详情
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteProduct(scope.row)"
              v-if="scope.row.status === 'INACTIVE'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 商品详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商品详情" width="800px">
      <div v-if="selectedProduct">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-image 
              :src="selectedProduct.imageUrl" 
              style="width: 100%"
              fit="cover"
            />
          </el-col>
          <el-col :span="16">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品ID">{{ selectedProduct.id }}</el-descriptions-item>
              <el-descriptions-item label="商品名称">{{ selectedProduct.name }}</el-descriptions-item>
              <el-descriptions-item label="分类">{{ selectedProduct.categoryName }}</el-descriptions-item>
              <el-descriptions-item label="商户">{{ selectedProduct.merchantName }}</el-descriptions-item>
              <el-descriptions-item label="价格">¥{{ selectedProduct.price }}</el-descriptions-item>
              <el-descriptions-item label="库存">{{ selectedProduct.stock }}</el-descriptions-item>
              <el-descriptions-item label="销量">{{ selectedProduct.sales }}</el-descriptions-item>
              <el-descriptions-item label="评分">{{ selectedProduct.rating || '暂无' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="selectedProduct.status === 'ACTIVE' ? 'success' : 'danger'">
                  {{ selectedProduct.status === 'ACTIVE' ? '上架' : '下架' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ selectedProduct.createTime }}</el-descriptions-item>
            </el-descriptions>
            
            <div style="margin-top: 20px;">
              <h4>商品描述</h4>
              <p>{{ selectedProduct.description || '暂无描述' }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const productList = ref([])
const categories = ref([])
const searchForm = ref({
  name: '',
  categoryId: '',
  merchantName: '',
  status: ''
})
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const detailDialogVisible = ref(false)
const selectedProduct = ref(null)

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    const response = await adminApi.getAllProducts(params)
    productList.value = response.data.records || []
    pagination.value.total = response.data.total || 0
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    // 这里应该调用获取分类的API
    // const response = await categoryApi.getCategories()
    // categories.value = response.data
    categories.value = [
      { id: 1, name: '主食套餐' },
      { id: 2, name: '面食类' },
      { id: 3, name: '汤品类' },
      { id: 4, name: '素食类' },
      { id: 5, name: '荤菜类' },
      { id: 6, name: '饮品类' }
    ]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const searchProducts = () => {
  pagination.value.page = 1
  loadProducts()
}

const resetSearch = () => {
  searchForm.value = {
    name: '',
    categoryId: '',
    merchantName: '',
    status: ''
  }
  pagination.value.page = 1
  loadProducts()
}

const viewProduct = (product) => {
  selectedProduct.value = product
  detailDialogVisible.value = true
}

const deleteProduct = async (product) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品 ${product.name} 吗？`, '确认删除')
    
    await adminApi.deleteProduct(product.id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error('删除商品失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadProducts()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  loadProducts()
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.admin-products {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
