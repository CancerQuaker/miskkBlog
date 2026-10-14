import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider, App as AntApp, theme as antdTheme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import App from './App';
import { useThemeStore } from './stores/themeStore';
import './styles/global.css';
import 'highlight.js/styles/github-dark.css';

dayjs.locale('zh-cn');

/** 根据 mode 解析出真实的 html data-theme（light/dark/eye） */
export function resolveTheme(mode: 'light' | 'dark' | 'eye' | 'system'): 'light' | 'dark' | 'eye' {
  if (mode !== 'system') return mode;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

const antdThemes: Record<string, any> = {
  light: {
    token: {
      colorPrimary: '#7b6cf0',
      borderRadius: 10,
      colorBgLayout: 'transparent',
      colorTextBase: '#38354a',
      fontSize: 14,
    },
  },
  dark: {
    algorithm: antdTheme.darkAlgorithm,
    token: {
      colorPrimary: '#9b8cff',
      borderRadius: 10,
      colorBgLayout: 'transparent',
      colorTextBase: '#f2eaf3',
      fontSize: 14,
    },
  },
  eye: {
    token: {
      colorPrimary: '#3a9a63',
      borderRadius: 10,
      colorBgContainer: '#e4f3e3',
      colorBgElevated: '#eaf6e9',
      colorBgLayout: 'transparent',
      colorTextBase: '#2f4436',
      colorBorderSecondary: 'rgba(47,68,54,0.1)',
      fontSize: 14,
    },
  },
};

function Root() {
  const mode = useThemeStore((s) => s.mode);
  // 跟随系统模式时，监听系统主题变化实时切换
  const [, force] = useState(0);
  useEffect(() => {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = () => force((n) => n + 1);
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, []);
  const resolved = resolveTheme(mode);
  document.documentElement.dataset.theme = resolved;

  return (
    <ConfigProvider locale={zhCN} theme={antdThemes[resolved]}>
      <AntApp>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </AntApp>
    </ConfigProvider>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>,
);
