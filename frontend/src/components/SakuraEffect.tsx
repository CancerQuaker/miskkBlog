import { useEffect, useRef } from 'react';

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

/** 樱花飘落特效（canvas-free，DOM 花瓣 + rAF） */
export default function SakuraEffect({ count = 18 }: { count?: number }) {
  const stageRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (window.innerWidth < 768) return;
    const stage = stageRef.current;
    if (!stage) return;

    const petals: Petal[] = [];
    const spawn = (initial: boolean): Petal => {
      const el = document.createElement('div');
      el.className = 'sakura-petal';
      el.innerHTML =
        '<svg viewBox="0 0 20 20" width="100%" height="100%"><path d="M10 1 C12 5 17 5 19 8 C15 10 15 14 13 18 C11 15 6 15 3 12 C7 11 7 6 10 1 Z" fill="#c3bcff"/></svg>';
      const size = 10 + Math.random() * 12;
      el.style.width = `${size}px`;
      el.style.height = `${size}px`;
      stage.appendChild(el);
      const p: Petal = {
        el,
        x: Math.random() * window.innerWidth,
        y: initial ? Math.random() * window.innerHeight : -30,
        size,
        vy: 0.6 + Math.random() * 1.1,
        vx: -0.6 + Math.random() * 1.2,
        rot: Math.random() * 360,
        vr: -1.5 + Math.random() * 3,
        opacity: 0.35 + Math.random() * 0.45,
      };
      el.style.opacity = String(p.opacity);
      return p;
    };

    for (let i = 0; i < count; i++) petals.push(spawn(true));

    let raf = 0;
    const tick = () => {
      for (const p of petals) {
        p.y += p.vy;
        p.x += p.vx + Math.sin((p.y + p.size) / 40) * 0.6;
        p.rot += p.vr;
        if (p.y > window.innerHeight + 30 || p.x < -40 || p.x > window.innerWidth + 40) {
          Object.assign(p, spawn(false), { el: p.el });
          p.el = petals[petals.indexOf(p)].el;
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
  }, [count]);

  return <div ref={stageRef} className="sakura-stage" aria-hidden />;
}
