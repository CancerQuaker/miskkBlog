import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

/** 星空闪烁特效（随机固定坐标 + 横幅同款 CSS 动画），外观面板可开关 */
export default function SakuraEffect({ count = 48 }: { count?: number }) {
  const sakura = useThemeStore((s) => s.sakura);
  const stageRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!sakura || window.innerWidth < 768) return;
    const stage = stageRef.current;
    if (!stage) return;

    const stars: HTMLDivElement[] = [];
    for (let i = 0; i < count; i++) {
      const el = document.createElement('div');
      const size = 12 + Math.random() * 10;
      el.className = 'starfield-star';
      el.style.left = `${Math.random() * 100}%`;
      el.style.top = `${Math.random() * 100}%`;
      el.style.width = `${size}px`;
      el.style.height = `${size}px`;
      el.style.animationDuration = '2.6s';
      el.style.animationDelay = `${-Math.random() * 2.6}s`;
      stage.appendChild(el);
      stars.push(el);
    }

    return () => stars.forEach((star) => star.remove());
  }, [sakura, count]);

  return <div ref={stageRef} className="starfield-stage" aria-hidden />;
}
