import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { viteStaticCopy } from 'vite-plugin-static-copy';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    viteStaticCopy({
      targets: [
        { src: 'node_modules/vditor/dist/js/i18n/zh_CN.js', dest: 'vditor/dist/js/i18n' },
        { src: 'node_modules/vditor/dist/js/lute/lute.min.js', dest: 'vditor/dist/js/lute' },
        { src: 'node_modules/vditor/dist/js/icons/ant.js', dest: 'vditor/dist/js/icons' },
        { src: 'node_modules/vditor/dist/js/highlight.js/highlight.min.js', dest: 'vditor/dist/js/highlight.js' },
        { src: 'node_modules/vditor/dist/js/highlight.js/third-languages.js', dest: 'vditor/dist/js/highlight.js' },
        { src: 'node_modules/vditor/dist/js/highlight.js/styles/github.min.css', dest: 'vditor/dist/js/highlight.js/styles' },
        { src: 'node_modules/vditor/dist/js/highlight.js/styles/github-dark.min.css', dest: 'vditor/dist/js/highlight.js/styles' },
        { src: 'node_modules/vditor/dist/css/content-theme/light.css', dest: 'vditor/dist/css/content-theme' },
        { src: 'node_modules/vditor/dist/css/content-theme/dark.css', dest: 'vditor/dist/css/content-theme' },
      ],
    }),
  ],
  server: {
    port: 5173,
    host: '0.0.0.0',
    // 允许局域网 IP 与 Cloudflare 快速隧道域名访问
    allowedHosts: ['.trycloudflare.com'],
    proxy: {
      // 全部 /api 请求代理到后端单体应用（8100）
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
          editor: ['vditor'],
          charts: ['echarts'],
        },
      },
    },
  },
});
