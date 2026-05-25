<template>
  <div class="theme-toggle" @click="toggleTheme" :title="getThemeTitle()">
    <el-icon>
      <Sunny v-if="themeStore.currentTheme === 'light'" />
      <Moon v-else-if="themeStore.currentTheme === 'dark'" />
      <Monitor v-else />
    </el-icon>
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'

const themeStore = useThemeStore()

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const getThemeTitle = () => {
  const themeNames = {
    light: '浅色模式',
    dark: '深色模式',
    auto: '跟随系统'
  }
  return `当前: ${themeNames[themeStore.theme]} (点击切换)`
}
</script>

<style scoped>
.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: var(--theme-bg-secondary, var(--el-bg-color));
  border: 2px solid var(--theme-border-primary, var(--el-border-color));
  color: var(--theme-text-primary, var(--el-text-color-primary));
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px var(--theme-shadow, rgba(0, 0, 0, 0.1));
  backdrop-filter: blur(10px);
}

.theme-toggle:hover {
  background-color: var(--theme-bg-tertiary, var(--el-fill-color-light));
  border-color: var(--theme-primary, var(--el-color-primary));
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 6px 20px var(--theme-shadow, rgba(0, 0, 0, 0.15));
}

.theme-toggle:active {
  transform: scale(0.95);
}

/* 图标动画 */
.theme-toggle .el-icon {
  transition: all 0.3s ease;
}

.theme-toggle:hover .el-icon {
  transform: rotate(180deg);
}

/* 深色模式特殊样式 */
.dark .theme-toggle {
  background-color: var(--theme-bg-secondary);
  border-color: var(--theme-border-primary);
  color: var(--theme-text-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.dark .theme-toggle:hover {
  background-color: var(--theme-bg-tertiary);
  border-color: var(--theme-primary);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.3);
}

/* 浅色模式特殊样式 */
.light .theme-toggle {
  background-color: rgba(255, 255, 255, 0.9);
  border-color: rgba(0, 0, 0, 0.1);
  color: #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.light .theme-toggle:hover {
  background-color: rgba(255, 255, 255, 1);
  border-color: var(--theme-primary);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.2);
}

@media (max-width: 768px) {
  .theme-toggle {
    top: 15px;
    right: 15px;
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .theme-toggle {
    top: 10px;
    right: 10px;
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
}
</style>