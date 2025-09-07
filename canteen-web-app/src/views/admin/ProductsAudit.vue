<template>
  <div class="products-audit">
    <!-- 搜索和筛选 -->
    <el-card style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索商品名称"
            @keyup.enter="loadProducts"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="审核状态" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.category" placeholder="商品分类" clearable>
            <el-option label="主食" value="staple" />
            <el-option label="菜品" value="dish" />
            <el-option label="汤品" value="soup" />
            <el-option label="饮品" value="drink" />
            <el-option label="小食" value="snack" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="loadProducts">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 商品列表 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>商品审核列表</span>
          <div>
            <el-button type="success" @click="batchApprove" :disabled="!selectedProducts.length">
              批量通过
            </el-button>
            <el-button type="danger" @click="batchReject" :disabled="!selectedProducts.length">
              批量拒绝
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="Array.isArray(products) ? products : []"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <el-image
              :src="scope.row.image"
              :preview-src-list="[scope.row.image]"
              style="width: 60px; height: 60px;"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <el-tag>{{ getCategoryName(scope.row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户" width="120" />
        <el-table-column prop="status" label="审核状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewProduct(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              type="success"
              size="small"
              @click="approveProduct(scope.row)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              type="danger"
              size="small"
              @click="rejectProduct(scope.row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: center;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </el-card>

    <!-- 商品详情对话框 -->
    <el-dialog v-model="detailVisible" title="商品详情" width="600px">
      <div v-if="currentProduct">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ getCategoryName(currentProduct.category) }}</el-descriptions-item>
          <el-descriptions-item label="价格">¥{{ currentProduct.price.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="商户">{{ currentProduct.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getStatusType(currentProduct.status)">
              {{ getStatusName(currentProduct.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentProduct.createdAt }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>商品描述</h4>
          <p>{{ currentProduct.description || '暂无描述' }}</p>
        </div>

        <div style="margin-top: 20px;">
          <h4>商品图片</h4>
          <el-image
            :src="currentProduct.image"
            style="width: 200px; height: 200px;"
            fit="cover"
          />
        </div>

        <div v-if="currentProduct.auditReason" style="margin-top: 20px;">
          <h4>审核意见</h4>
          <p>{{ currentProduct.auditReason }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="auditType === 'approve' ? '通过审核' : '拒绝审核'" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="auditType === 'approve' ? '请输入通过理由（可选）' : '请输入拒绝理由'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin.js'

const loading = ref(false)
const products = ref([])
const selectedProducts = ref([])
const detailVisible = ref(false)
const auditVisible = ref(false)
const currentProduct = ref(null)
const auditType = ref('approve')

const searchForm = ref({
  keyword: '',
  status: '',
  category: ''
})

const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const auditForm = ref({
  reason: ''
})

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    }
    
    const response = await adminApi.getAllProducts(params)
    products.value = response.data?.list || []
    pagination.value.total = response.data?.total || 0
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    status: '',
    category: ''
  }
  pagination.value.page = 1
  loadProducts()
}

const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

const viewProduct = (product) => {
  currentProduct.value = product
  detailVisible.value = true
}

const approveProduct = (product) => {
  currentProduct.value = product
  auditType.value = 'approve'
  auditForm.value.reason = ''
  auditVisible.value = true
}

const rejectProduct = (product) => {
  currentProduct.value = product
  auditType.value = 'reject'
  auditForm.value.reason = ''
  auditVisible.value = true
}

const confirmAudit = async () => {
  try {
    const approved = auditType.value === 'approve'
    await adminApi.approveProduct(currentProduct.value.id, approved, auditForm.value.reason)
    
    ElMessage.success(`商品${approved ? '通过' : '拒绝'}审核成功`)
    auditVisible.value = false
    loadProducts()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const batchApprove = async () => {
  try {
    await ElMessageBox.confirm('确认批量通过选中的商品吗？', '批量审核', {
      type: 'warning'
    })
    
    for (const product of selectedProducts.value) {
      await adminApi.approveProduct(product.id, true, '批量审核通过')
    }
    
    ElMessage.success('批量审核通过成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

const batchReject = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝理由', '批量拒绝', {
      inputType: 'textarea'
    })
    
    for (const product of selectedProducts.value) {
      await adminApi.approveProduct(product.id, false, reason)
    }
    
    ElMessage.success('批量拒绝成功')
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error('批量拒绝失败')
    }
  }
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

const getStatusName = (status) => {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.products-audit {
  padding: 20px;
}
</style>
