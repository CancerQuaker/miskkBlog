import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

/** 80% 光尘分布在左右边缘，20% 点缀中央阅读区 */
export function randomDustX(random: () => number): number {
  const region = random();
  if (region < 0.4) return 3 + random() * 22;
  if (region < 0.8) return 75 + random() * 22;
  return 32 + random() * 36;
}

/** 柔光尘背景：细小圆点缓慢漂浮，滚动时带轻微视差，外观面板可开关 */
export default function SakuraEffect({ count = 64 }: { count?: number }) {
  const sakura = useThemeStore((s) => s.sakura);
  const stageRef = useRef<HTMLDivElement>(null);
  const layerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!sakura || window.innerWidth < 768) return;
    const stage = stageRef.current;
    const layer = layerRef.current;
    if (!stage || !layer) return;

    const particles: HTMLDivElement[] = [];
    for (let i = 0; i < count; i++) {
      const el = document.createElement('div');
      const size = 2 + Math.random() * 3;
      const duration = 14 + Math.random() * 10;
      el.className = 'starfield-dust';
      el.style.left = `${randomDustX(Math.random)}%`;
      el.style.top = `${Math.random() * 100}%`;
      el.style.width = `${size}px`;
      el.style.height = `${size}px`;
      el.style.setProperty('--dust-drift-x', `${-14 + Math.random() * 28}px`);
      el.style.setProperty('--dust-opacity', `${0.45 + Math.random() * 0.25}`);
      el.style.animationDuration = `${duration}s`;
      el.style.animationDelay = `${-Math.random() * duration}s`;
      layer.appendChild(el);
      particles.push(el);
    }

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    let frame = 0;
    const updateParallax = () => {
      frame = 0;
      layer.style.transform = `translate3d(0, ${window.scrollY * 0.035}px, 0)`;
    };
    const onScroll = () => {
      if (!frame) frame = window.requestAnimationFrame(updateParallax);
    };
    if (!reduceMotion) {
      updateParallax();
      window.addEventListener('scroll', onScroll, { passive: true });
    }

    return () => {
      window.removeEventListener('scroll', onScroll);
      if (frame) window.cancelAnimationFrame(frame);
      particles.forEach((el) => el.remove());
    };
  }, [sakura, count]);

  return (
    <div ref={stageRef} className="starfield-stage" aria-hidden>
      <div ref={layerRef} className="starfield-dust-layer" />
    </div>
  );
}
