import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 本机（以太网适配器）局域网 IP，换网络/换电脑时只改这一处
const LOCAL_IP = '192.168.68.53'
const BACKEND_PORT = 8080
const FRONTEND_PORT = 5173

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    // 监听 0.0.0.0：本机用 localhost 或 IP 都能开，同一局域网的其他设备也能访问
    host: '0.0.0.0',
    port: FRONTEND_PORT,
    strictPort: true,
    // 开发阶段跨域代理：/api -> 后端
    proxy: {
      '/api': {
        target: `http://${LOCAL_IP}:${BACKEND_PORT}`,
        changeOrigin: true
      }
    }
  },
  // 打包后如果用相对路径部署，可保持默认 '/'；此处仅作说明
  build: {
    outDir: 'dist'
  }
})
