import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    theme: localStorage.getItem('theme') || 'light',
    systemTheme: 'light'
  }),
  
  getters: {
    currentTheme: (state) => {
      if (state.theme === 'auto') {
        return state.systemTheme
      }
      return state.theme
    },
    isDark: (state) => {
      return state.currentTheme === 'dark'
    }
  },
  
  actions: {
    setTheme(theme) {
      this.theme = theme
      localStorage.setItem('theme', theme)
      this.applyTheme()
    },
    
    initTheme() {
      // 检测系统主题
      this.detectSystemTheme()
      
      // 监听系统主题变化
      if (window.matchMedia) {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
        mediaQuery.addEventListener('change', (e) => {
          this.systemTheme = e.matches ? 'dark' : 'light'
          if (this.theme === 'auto') {
            this.applyTheme()
          }
        })
      }
      
      // 应用当前主题
      this.applyTheme()
    },
    
    detectSystemTheme() {
      if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        this.systemTheme = 'dark'
      } else {
        this.systemTheme = 'light'
      }
    },
    
    applyTheme() {
      const html = document.documentElement
      const body = document.body
      
      // 移除所有主题类
      html.classList.remove('light', 'dark')
      body.classList.remove('light', 'dark')
      
      // 应用当前主题
      const currentTheme = this.currentTheme
      html.classList.add(currentTheme)
      body.classList.add(currentTheme)
      
      // 设置CSS变量
      if (currentTheme === 'dark') {
        html.style.setProperty('--el-bg-color', '#1a1a1a')
        html.style.setProperty('--el-bg-color-page', '#0a0a0a')
        html.style.setProperty('--el-text-color-primary', '#e5eaf3')
        html.style.setProperty('--el-text-color-regular', '#cfd3dc')
        html.style.setProperty('--el-border-color', '#414243')
        html.style.setProperty('--el-fill-color-blank', '#262727')
      } else {
        html.style.setProperty('--el-bg-color', '#ffffff')
        html.style.setProperty('--el-bg-color-page', '#f2f3f5')
        html.style.setProperty('--el-text-color-primary', '#303133')
        html.style.setProperty('--el-text-color-regular', '#606266')
        html.style.setProperty('--el-border-color', '#dcdfe6')
        html.style.setProperty('--el-fill-color-blank', '#ffffff')
      }
      
      // 触发主题变化事件
      window.dispatchEvent(new CustomEvent('theme-changed', { 
        detail: { theme: currentTheme } 
      }))
    },
    
    toggleTheme() {
      const themes = ['light', 'dark', 'auto']
      const currentIndex = themes.indexOf(this.theme)
      const nextIndex = (currentIndex + 1) % themes.length
      this.setTheme(themes[nextIndex])
    }
  }
})