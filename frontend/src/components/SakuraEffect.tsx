import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

interface DustParticle {
  baseX: number;
  baseY: number;
  x: number;
  y: number;
  radius: number;
  maxAlpha: number;
  phase: number;
  driftSpeed: number;
  driftX: number;
  driftY: number;
  orbitAngle: number;
  orbitRadius: number;
  orbitSpeed: number;
}

const TAU = Math.PI * 2;
/** 卡片与浮层保持环境漂浮，只有页面背景及留白触发指针牵引 */
const NON_BACKGROUND_SELECTOR = [
  '.card',
  '.ant-card',
  '.navbar',
  '.hero',
  '.site-footer',
  '.float-panel',
  '.ant-popover',
  '.ant-dropdown',
  '.ctx-menu',
  '#oml2d-stage',
].join(', ');

/** 80% 光尘分布在左右边缘，20% 点缀中央阅读区 */
export function randomDustX(random: () => number): number {
  const region = random();
  if (region < 0.4) return 3 + random() * 22;
  if (region < 0.8) return 75 + random() * 22;
  return 32 + random() * 36;
}

/** Canvas 柔光尘：卡片区域随机漂浮，页面背景以鼠标为中心牵引 */
export default function SakuraEffect({ count = 64 }: { count?: number }) {
  const sakura = useThemeStore((s) => s.sakura);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!sakura || window.innerWidth < 768) return;
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!canvas || !ctx) return;

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const pointer = { x: 0, y: 0, active: false, attract: false };
    let width = window.innerWidth;
    let height = window.innerHeight;
    let primary = '';
    let primaryLight = '';
    let particles: DustParticle[] = [];
    let pull = 0;
    let frame = 0;
    let lastTime = performance.now();
    let lastScrollY = window.scrollY;

    const readColors = () => {
      const style = getComputedStyle(document.documentElement);
      primary = style.getPropertyValue('--primary').trim();
      primaryLight = style.getPropertyValue('--primary-light').trim();
    };

    const createParticles = () => Array.from({ length: count }, () => {
      const baseX = randomDustX(Math.random) / 100 * width;
      const baseY = Math.random() * height;
      return {
        baseX,
        baseY,
        x: baseX,
        y: baseY,
        radius: 1 + Math.random() * 1.5,
        maxAlpha: 0.45 + Math.random() * 0.25,
        phase: Math.random() * TAU,
        driftSpeed: 0.2 + Math.random() * 0.35,
        driftX: 8 + Math.random() * 16,
        driftY: 10 + Math.random() * 20,
        orbitAngle: Math.random() * TAU,
        orbitRadius: 30 + Math.random() * 145,
        orbitSpeed: (0.25 + Math.random() * 0.5) * (Math.random() < 0.5 ? -1 : 1),
      };
    });

    const draw = (time: number) => {
      ctx.clearRect(0, 0, width, height);
      ctx.fillStyle = primary;
      ctx.shadowColor = primaryLight;
      ctx.shadowBlur = 7;
      for (const p of particles) {
        const pulse = 0.5 + 0.5 * Math.sin(time * 0.8 + p.phase);
        ctx.globalAlpha = Math.min(0.85, (0.18 + (p.maxAlpha - 0.18) * pulse) * (1 + pull * 0.15));
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.radius * (1 + pull * 0.12), 0, TAU);
        ctx.fill();
      }
      ctx.globalAlpha = 1;
      ctx.shadowBlur = 0;
    };

    const resize = () => {
      const bounds = canvas.getBoundingClientRect();
      width = bounds.width;
      height = bounds.height;
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      canvas.width = Math.round(width * dpr);
      canvas.height = Math.round(height * dpr);
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      particles = width < 768 ? [] : createParticles();
      if (reduceMotion) draw(performance.now() / 1000);
    };

    const updatePointerMode = (target: Element | null) => {
      pointer.attract = pointer.active && !!target && !target.closest(NON_BACKGROUND_SELECTOR);
    };
    const onPointerMove = (event: PointerEvent) => {
      if (event.pointerType === 'touch') return;
      pointer.x = event.clientX;
      pointer.y = event.clientY;
      pointer.active = true;
      const target = event.target instanceof Element
        ? event.target
        : document.elementFromPoint(event.clientX, event.clientY);
      updatePointerMode(target);
    };
    const disablePointer = () => {
      pointer.active = false;
      pointer.attract = false;
    };
    const onScroll = () => {
      const delta = window.scrollY - lastScrollY;
      lastScrollY = window.scrollY;
      for (const p of particles) {
        p.baseY -= delta * 0.06;
        p.y -= delta * 0.06;
        if (p.baseY < -40) {
          p.baseY += height + 80;
          p.y += height + 80;
        } else if (p.baseY > height + 40) {
          p.baseY -= height + 80;
          p.y -= height + 80;
        }
      }
      if (pointer.active) updatePointerMode(document.elementFromPoint(pointer.x, pointer.y));
    };

    const animate = (now: number) => {
      const dt = Math.min((now - lastTime) / 1000, 0.05);
      const step = Math.min(dt * 60, 2);
      const time = now / 1000;
      lastTime = now;
      pull += ((pointer.attract ? 1 : 0) - pull) * (1 - Math.pow(0.9, step));

      for (const p of particles) {
        p.orbitAngle += p.orbitSpeed * dt * (0.25 + pull);
        const ambientX = p.baseX + Math.sin(time * p.driftSpeed + p.phase) * p.driftX;
        const ambientY = p.baseY + Math.cos(time * p.driftSpeed * 0.8 + p.phase) * p.driftY;
        const orbitX = pointer.x + Math.cos(p.orbitAngle) * p.orbitRadius;
        const orbitY = pointer.y + Math.sin(p.orbitAngle) * p.orbitRadius * 0.68;
        const targetX = ambientX + (orbitX - ambientX) * pull;
        const targetY = ambientY + (orbitY - ambientY) * pull;
        const ease = 1 - Math.pow(1 - (0.018 + pull * 0.065), step);
        p.x += (targetX - p.x) * ease;
        p.y += (targetY - p.y) * ease;
      }

      draw(time);
      frame = window.requestAnimationFrame(animate);
    };

    readColors();
    resize();
    const themeObserver = new MutationObserver(() => {
      readColors();
      if (reduceMotion) draw(performance.now() / 1000);
    });
    themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme', 'style'] });
    window.addEventListener('resize', resize);

    if (!reduceMotion) {
      window.addEventListener('pointermove', onPointerMove, { passive: true });
      window.addEventListener('blur', disablePointer);
      window.addEventListener('scroll', onScroll, { passive: true });
      document.documentElement.addEventListener('mouseleave', disablePointer);
      frame = window.requestAnimationFrame(animate);
    }

    return () => {
      window.cancelAnimationFrame(frame);
      window.removeEventListener('resize', resize);
      window.removeEventListener('pointermove', onPointerMove);
      window.removeEventListener('blur', disablePointer);
      window.removeEventListener('scroll', onScroll);
      document.documentElement.removeEventListener('mouseleave', disablePointer);
      themeObserver.disconnect();
      ctx.setTransform(1, 0, 0, 1, 0, 0);
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    };
  }, [sakura, count]);

  return <canvas ref={canvasRef} className="starfield-stage" aria-hidden />;
}
