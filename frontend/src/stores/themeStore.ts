import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type ThemeMode = 'light' | 'dark' | 'eye' | 'system';

/** 主题色预设（星紫为默认，null 表示跟随主题内置色） */
export const ACCENT_PRESETS: { color: string | null; name: string }[] = [
  { color: null, name: '默认' },
  { color: '#7b6cf0', name: '星紫' },
  { color: '#f07ba0', name: '樱粉' },
  { color: '#5696ff', name: '天蓝' },
  { color: '#3fbf7f', name: '抹茶' },
  { color: '#f6a522', name: '蜜橙' },
  { color: '#e85d75', name: '绯红' },
  { color: '#4ecbe8', name: '湖青' },
];

interface ThemeState {
  mode: ThemeMode;
  /** 自定义主题色（hex），null 表示用各主题内置色 */
  accent: string | null;
  /** 特效开关：樱花飘落物 / 星星鼠标样式 / Live2D 看板娘 */
  sakura: boolean;
  starCursor: boolean;
  live2d: boolean;
  setMode: (mode: ThemeMode) => void;
  setAccent: (accent: string | null) => void;
  setSakura: (on: boolean) => void;
  setStarCursor: (on: boolean) => void;
  setLive2d: (on: boolean) => void;
}

/** 主题模式：白 / 暗 / 护眼 / 跟随系统；主题色与特效开关可自定义 */
export const useThemeStore = create<ThemeState>()(
  persist(
    (set) => ({
      mode: 'system',
      accent: null,
      sakura: true,
      starCursor: true,
      live2d: true,
      setMode: (mode) => set({ mode }),
      setAccent: (accent) => set({ accent }),
      setSakura: (on) => set({ sakura: on }),
      setStarCursor: (on) => set({ starCursor: on }),
      setLive2d: (on) => set({ live2d: on }),
    }),
    { name: 'miskk-theme' },
  ),
);

/* ---------- 主题色派生工具 ---------- */

function hexToRgb(hex: string): [number, number, number] {
  const m = hex.replace('#', '');
  const full = m.length === 3 ? m.split('').map((c) => c + c).join('') : m;
  const n = parseInt(full, 16);
  return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
}

/** amount > 0 变亮、< 0 变暗（-1 ~ 1） */
export function shadeHex(hex: string, amount: number): string {
  const [r, g, b] = hexToRgb(hex);
  const f = (v: number) =>
    Math.round(amount >= 0 ? v + (255 - v) * amount : v * (1 + amount));
  return `#${[f(r), f(g), f(b)].map((v) => v.toString(16).padStart(2, '0')).join('')}`;
}

export function hexToRgba(hex: string, alpha: number): string {
  const [r, g, b] = hexToRgb(hex);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}
