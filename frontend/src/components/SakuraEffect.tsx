import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

interface Petal {
  el: HTMLDivElement;
  x: number;
  y: number;
  size: number;
  vy: number;
  vx: number;
  rot: number;
  vr: number;
  opacity: number;
}

const BASE_FRAME_MS = 1000 / 60;
const MAX_FRAME_STEP = 3;

/** 按实际帧间隔换算运动步长，后台恢复时最多补 3 帧，避免花瓣瞬移 */
export function getAnimationStep(elapsedMs: number): number {
  return Math.min(Math.max(elapsedMs, 0) / BASE_FRAME_MS, MAX_FRAME_STEP);
}

/** 深度越大，花瓣越清晰、略大且下落稍快，形成克制的远近层次 */
export function createPetalMotion(
  depth: number,
  random: () => number,
): Pick<Petal, 'size' | 'vy' | 'vx' | 'vr' | 'opacity'> {
  const d = Math.min(Math.max(depth, 0), 1);
  return {
    size: 7 + d * 8,
    vy: 0.28 + d * 0.42,
    vx: (random() - 0.5) * 0.3,
    vr: (random() - 0.5) * 0.8,
    opacity: 0.12 + d * 0.16,
  };
}

/** 樱花飘落特效（canvas-free，DOM 花瓣 + rAF），外观面板可开关 */
export default function SakuraEffect({ count = 12 }: { count?: number }) {
  const sakura = useThemeStore((s) => s.sakura);
  const stageRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!sakura || window.innerWidth < 768) return;
    const stage = stageRef.current;
    if (!stage) return;

    const resetPetal = (p: Petal, initial: boolean) => {
      Object.assign(p, createPetalMotion(Math.random(), Math.random), {
        x: Math.random() * window.innerWidth,
        y: initial ? Math.random() * window.innerHeight : -30,
        rot: Math.random() * 360,
      });
      p.el.style.width = `${p.size}px`;
      p.el.style.height = `${p.size}px`;
      p.el.style.opacity = String(p.opacity);
    };

    const spawn = (initial: boolean): Petal => {
      const el = document.createElement('div');
      el.className = 'sakura-petal';
      el.innerHTML =
        '<svg viewBox="0 0 20 20" width="100%" height="100%" aria-hidden="true">' +
        '<path d="M10 19 C6 17 3 13 3 9 C3 5 6 2 9 4 L10 6 L11 4 C14 2 17 5 17 9 C17 13 14 17 10 19 Z" fill="#efa4ba"/>' +
        '<path d="M9.5 6.5 C7.5 8.5 7.2 12 9 15.5" fill="none" stroke="#ffd8e2" strokeWidth="1.1" strokeLinecap="round" opacity=".7"/>' +
        '</svg>';
      stage.appendChild(el);
      const p: Petal = { el, x: 0, y: 0, size: 7, vy: 0, vx: 0, rot: 0, vr: 0, opacity: 0 };
      resetPetal(p, initial);
      return p;
    };

    const petals: Petal[] = [];
    for (let i = 0; i < count; i++) petals.push(spawn(true));

    let raf = 0;
    let previousTime: number | null = null;
    const tick = (now: number) => {
      const step = previousTime === null ? 1 : getAnimationStep(now - previousTime);
      previousTime = now;
      for (const p of petals) {
        p.y += p.vy * step;
        p.x += (p.vx + Math.sin((p.y + p.size) / 70) * 0.22) * step;
        p.rot += p.vr * step;
        if (p.y > window.innerHeight + 30 || p.x < -40 || p.x > window.innerWidth + 40) {
          resetPetal(p, false);
        }
        p.el.style.transform = `translate(${p.x}px, ${p.y}px) rotate(${p.rot}deg)`;
      }
      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);

    return () => {
      cancelAnimationFrame(raf);
      petals.forEach((p) => p.el.remove());
    };
  }, [sakura, count]);

  return <div ref={stageRef} className="sakura-stage" aria-hidden />;
}
