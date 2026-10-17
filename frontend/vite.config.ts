import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    host: '0.0.0.0',
    // 允许局域网 IP 与 Cloudflare 快速隧道域名访问
    allowedHosts: ['.trycloudflare.com'],
    proxy: {
      // 全部请求走网关（8100），网关再按 /api/xxx 分发到各微服务
      '/api': {
        target: 'http://localhost:8100',
        changeOrigin: true,
      },
    },
  },
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          antd: ['antd', '@ant-design/icons'],
          editor: ['@uiw/react-md-editor'],
          charts: ['echarts'],
        },
      },
    },
  },
});
