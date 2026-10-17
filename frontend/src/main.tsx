import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ConfigProvider, App as AntApp, theme as antdTheme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import App from './App';
import { useThemeStore, shadeHex, hexToRgba, resolveTheme } from './stores/themeStore';
import './styles/global.css';
import 'highlight.js/styles/github-dark.css';

dayjs.locale('zh-cn');

const antdThemes: Record<string, any> = {
  light: {
    token: {
      colorPrimary: '#80a942',
      borderRadius: 10,
      colorBgLayout: 'transparent',
      colorTextBase: '#38354a',
      fontSize: 14,
    },
  },
  dark: {
    algorithm: antdTheme.darkAlgorithm,
    token: {
      colorPrimary: '#97b864',
      borderRadius: 10,
      colorBgLayout: 'transparent',
      colorTextBase: '#f2eaf3',
      fontSize: 14,
    },
  },
  eye: {
    token: {
      colorPrimary: '#80a942',
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
  const accent = useThemeStore((s) => s.accent);
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

  // 自定义主题色：以行内 CSS 变量覆盖各主题内置的 --primary 系列
  useEffect(() => {
    const root = document.documentElement;
    if (!accent) {
      root.style.removeProperty('--primary');
      root.style.removeProperty('--primary-strong');
      root.style.removeProperty('--primary-weak');
      root.style.removeProperty('--primary-light');
      return;
    }
    root.style.setProperty('--primary', accent);
    root.style.setProperty('--primary-strong', shadeHex(accent, resolved === 'dark' ? 0.12 : -0.1));
    root.style.setProperty('--primary-weak', hexToRgba(accent, 0.14));
    // 浅端供星星光标等渐变高光使用
    root.style.setProperty('--primary-light', shadeHex(accent, resolved === 'dark' ? 0.3 : 0.28));
  }, [accent, resolved]);

  const baseTheme = antdThemes[resolved];
  const antdThemeApplied = accent
    ? { ...baseTheme, token: { ...baseTheme.token, colorPrimary: accent } }
    : baseTheme;

  return (
    <ConfigProvider locale={zhCN} theme={antdThemeApplied}>
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
