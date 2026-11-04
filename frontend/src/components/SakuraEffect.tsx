import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

/** 星空闪烁特效（随机固定坐标 + CSS 透明度动画），外观面板可开关 */
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
      const depth = Math.random();
      const size = 12 + depth * 10;
      const peakOpacity = 0.18 + depth * 0.24;
      const duration = 3 + Math.random() * 4;
      el.className = 'starfield-star';
      el.style.left = `${Math.random() * 100}%`;
      el.style.top = `${Math.random() * 100}%`;
      el.style.width = `${size}px`;
      el.style.height = `${size}px`;
      el.style.transform = `rotate(${Math.random() * 45}deg)`;
      el.style.setProperty('--star-min', String(peakOpacity * 0.22));
      el.style.setProperty('--star-peak', String(peakOpacity));
      el.style.animationDuration = `${duration}s`;
      el.style.animationDelay = `${-Math.random() * duration}s`;
      el.innerHTML =
        '<svg viewBox="0 0 24 24" width="100%" height="100%" aria-hidden="true">' +
        '<path d="M12 1 L15.1 8.9 L23 12 L15.1 15.1 L12 23 L8.9 15.1 L1 12 L8.9 8.9 Z" style="fill: var(--primary)"/>' +
        '<path d="M12 5 L13.8 10.2 L19 12 L13.8 13.8 L12 19 L10.2 13.8 L5 12 L10.2 10.2 Z" style="fill: var(--primary-light)" opacity=".45"/>' +
        '</svg>';
      stage.appendChild(el);
      stars.push(el);
    }

    return () => stars.forEach((star) => star.remove());
  }, [sakura, count]);

  return <div ref={stageRef} className="starfield-stage" aria-hidden />;
}
