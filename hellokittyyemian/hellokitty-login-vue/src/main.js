import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import './style.css'
// 首页美化增强 + 全局消息提示样式（纯新增层，删除这行即可回退）
import './styles/beauty.css'

createApp(App).use(router).mount('#app')
