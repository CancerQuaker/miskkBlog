import { useEffect, useRef, useState } from 'react';

/** 左键点击掉落的文字（社会主义核心价值观） */
const CLICK_WORDS = [
  '富强', '民主', '文明', '和谐',
  '自由', '平等', '公正', '法治',
  '爱国', '敬业', '诚信', '友善',
];

/** 蹦字配色（跟随主题的糖果色） */
const WORD_COLORS = ['#7b6cf0', '#5696ff', '#9d6bf5', '#f6a522', '#3fbf7f', '#4ecbe8'];

/** 拖尾小星星的颜色 */
const SPARK_COLORS = ['#7b6cf0', '#5696ff', '#b9adff', '#8fd0ff'];

/**
 * 魔法星星鼠标指针：
 * - 启用后隐藏系统光标，用一颗缓慢旋转的四角星代替（比系统光标稍大）
 * - 鼠标移动时沿途洒落小星星拖尾
 * - 左键：星星收缩旋转 + 在点击处向下掉落「富强/民主/文明/和谐…」彩色文字 + 星形气泡「✦」
 * - 右键：交给自定义右键菜单组件（见 ContextMenu.tsx）
 * - 仅在桌面端（pointer: fine）启用
 */
export default function StarCursor() {
  const [enabled, setEnabled] = useState(false);
  const [clicking, setClicking] = useState(false);
  const starRef = useRef<HTMLDivElement>(null);
  const wordLayerRef = useRef<HTMLDivElement>(null);
  const sparkLayerRef = useRef<HTMLDivElement>(null);
  const pos = useRef({ x: -100, y: -100 });
  const wordSeq = useRef(0);
  const sparkSeq = useRef(0);
  const lastSpark = useRef(0);

  useEffect(() => {
    const fine = window.matchMedia('(pointer: fine)').matches;
    const wide = window.innerWidth >= 768;
    if (!fine || !wide) return;
    setEnabled(true);
    document.documentElement.classList.add('star-cursor-on');
    return () => document.documentElement.classList.remove('star-cursor-on');
  }, []);

  useEffect(() => {
    if (!enabled) return;

    let raf = 0;
    const onMove = (e: MouseEvent) => {
      pos.current = { x: e.clientX, y: e.clientY };
      // 移动拖尾：每隔 ~36ms 洒一颗小星星
      const now = performance.now();
      if (now - lastSpark.current > 36) {
        lastSpark.current = now;
        spawnSpark(e.clientX, e.clientY);
      }
    };

    const loop = () => {
      if (starRef.current) {
        starRef.current.style.left = `${pos.current.x}px`;
        starRef.current.style.top = `${pos.current.y}px`;
      }
      raf = requestAnimationFrame(loop);
    };

    const spawnSpark = (x: number, y: number) => {
      const layer = sparkLayerRef.current;
      if (!layer || layer.childElementCount > 60) return;
      const el = document.createElement('div');
      el.className = 'star-spark';
      const size = 5 + Math.random() * 7;
      const color = SPARK_COLORS[sparkSeq.current++ % SPARK_COLORS.length];
      el.style.left = `${x + (Math.random() * 16 - 8)}px`;
      el.style.top = `${y + (Math.random() * 16 - 8)}px`;
      el.innerHTML =
        `<svg viewBox="0 0 20 20" width="${size}" height="${size}">` +
        `<path d="M10 0 L12 8 L20 10 L12 12 L10 20 L8 12 L0 10 L8 8 Z" fill="${color}"/></svg>`;
      layer.appendChild(el);
      el.addEventListener('animationend', () => el.remove());
      setTimeout(() => el.remove(), 900);
    };

    /** 在点击处向下掉落文字并消散 */
    const spawnWord = (x: number, y: number, text: string) => {
      const layer = wordLayerRef.current;
      if (!layer) return;
      const el = document.createElement('div');
      el.className = 'star-click-word';
      el.textContent = text;
      const color = WORD_COLORS[wordSeq.current % WORD_COLORS.length];
      wordSeq.current += 1;
      el.style.left = `${x}px`;
      el.style.top = `${y}px`;
      el.style.color = color;
      el.style.textShadow = `0 0 8px ${color}55`;
      layer.appendChild(el);
      el.addEventListener('animationend', () => el.remove());
      setTimeout(() => el.remove(), 1600);
    };

    const onDown = (e: MouseEvent) => {
      if (e.button !== 0) return;
      setClicking(true);
      const word = CLICK_WORDS[Math.floor(Math.random() * CLICK_WORDS.length)];
      spawnWord(e.clientX, e.clientY + 14, word);
      // 点击瞬间炸开一圈小星星
      for (let i = 0; i < 6; i++) spawnSpark(e.clientX, e.clientY);
    };
    const onUp = (e: MouseEvent) => {
      if (e.button === 0) setClicking(false);
    };

    window.addEventListener('mousemove', onMove);
    window.addEventListener('mousedown', onDown);
    window.addEventListener('mouseup', onUp);
    raf = requestAnimationFrame(loop);

    return () => {
      window.removeEventListener('mousemove', onMove);
      window.removeEventListener('mousedown', onDown);
      window.removeEventListener('mouseup', onUp);
      cancelAnimationFrame(raf);
    };
  }, [enabled]);

  if (!enabled) return null;

  return (
    <div className="star-stage" aria-hidden>
      <div ref={starRef} className={`star-cursor ${clicking ? 'clicking' : ''}`}>
        <svg viewBox="0 0 26 26" width="26" height="26">
          <defs>
            <linearGradient id="starGrad" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stopColor="#b9adff" />
              <stop offset="100%" stopColor="#6554e8" />
            </linearGradient>
          </defs>
          <path
            d="M13 1 L15.6 10.4 L25 13 L15.6 15.6 L13 25 L10.4 15.6 L1 13 L10.4 10.4 Z"
            fill="url(#starGrad)"
            stroke="#fff"
            strokeWidth="1"
            strokeLinejoin="round"
          />
        </svg>
      </div>
      <div ref={sparkLayerRef} className="star-spark-layer" />
      <div ref={wordLayerRef} className="star-word-layer" />
    </div>
  );
}
