<template>
  <div class="home">
    <div class="hero-section">
      <el-card class="hero-card" shadow="never">
        <div class="hero-content">
          <h1>欢迎来到高校食堂订餐系统</h1>
          <p>美味佳肴，一键下单，享受便捷的校园用餐体验</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="$router.push('/products')">
              <el-icon><Shop /></el-icon>
              开始点餐
            </el-button>
            <el-button size="large" @click="$router.push('/login')" v-if="!isLoggedIn">
              <el-icon><User /></el-icon>
              登录账号
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <div class="features-section">
      <h2>系统特色</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-icon">
              <el-icon size="48" color="#409EFF"><Search /></el-icon>
            </div>
            <h3>智能搜索</h3>
            <p>支持商品名称搜索、分类筛选、价格排序等多种筛选方式</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-icon">
              <el-icon size="48" color="#67C23A"><ShoppingCart /></el-icon>
            </div>
            <h3>便捷购物车</h3>
            <p>实时计算价格，支持数量调整，一键清空，购物体验流畅</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-icon">
              <el-icon size="48" color="#E6A23C"><Clock /></el-icon>
            </div>
            <h3>订单跟踪</h3>
            <p>详细的订单进度时间线，实时了解订单状态和预计完成时间</p>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="stats-section">
      <el-card shadow="hover">
        <h2>系统数据</h2>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">50+</div>
              <div class="stat-label">精选菜品</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">1000+</div>
              <div class="stat-label">服务用户</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">5000+</div>
              <div class="stat-label">完成订单</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">98%</div>
              <div class="stat-label">满意度</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { Shop, User, Search, ShoppingCart, Clock } from '@element-plus/icons-vue'

export default {
  name: 'Home',
  components: {
    Shop, User, Search, ShoppingCart, Clock
  },
  setup() {
    const isLoggedIn = computed(() => {
      return !!localStorage.getItem('token')
    })
    
    return {
      isLoggedIn
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  margin-bottom: 40px;
}

.hero-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.hero-content {
  text-align: center;
  padding: 60px 20px;
  color: white;
}

.hero-content h1 {
  font-size: 2.5em;
  margin-bottom: 20px;
  font-weight: bold;
}

.hero-content p {
  font-size: 1.2em;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  gap: 30px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 30px;
}

.hero-actions .el-button {
  min-width: 140px;
  padding: 12px 24px;
}

.features-section {
  margin-bottom: 40px;
}

.features-section h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 2em;
}

.feature-card {
  text-align: center;
  padding: 20px;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.feature-icon {
  margin-bottom: 15px;
}

.feature-card h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.feature-card p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.stats-section {
  margin-bottom: 40px;
}

.stats-section h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 2.5em;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  color: #606266;
  font-size: 1.1em;
}

@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 2em;
  }
  
  .hero-content p {
    font-size: 1em;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>