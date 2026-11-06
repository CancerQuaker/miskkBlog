import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

type ParticleMode = 'ambient' | 'attracted';

interface DustParticle {
  mode: ParticleMode;
  baseX: number;
  baseY: number;
  regionRadiusX: number;
  regionRadiusY: number;
  x: number;
  y: number;
  pull: number;
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
const ATTRACTED_COUNT = 16;
const PARTICLE_LINK_DISTANCE = 110;
const PARTICLE_REPULSION_RADIUS = 36;
const PARTICLE_REPULSION_STRENGTH = 1.4;
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

type DustRegion = 'left' | 'center' | 'right';

/** 根据 CSS 视口面积计算粒子数量，避免高 DPI 屏幕重复增加粒子 */
export function getDynamicParticleCount(width: number, height: number): number {
  return Math.max(64, Math.min(240, Math.round(width * height / 7200)));
}

/** 左右数量尽量相等，中央区域仅保留约 10% 粒子 */
function dustRegionAt(index: number, total: number): DustRegion {
  const centerCount = Math.floor(total * 0.1);
  const leftCount = Math.floor((total - centerCount) / 2);
  if (index < leftCount) return 'left';
  if (index < total - centerCount) return 'right';
  return 'center';
}

export function randomDustX(random: () => number, region: DustRegion): number {
  if (region === 'left') return 3 + random() * 22;
  if (region === 'right') return 75 + random() * 22;
  return 32 + random() * 36;
}

/** Canvas 柔光尘：数量随视口调整，粒子在独立椭圆区域运动，最近 16 个可被牵引 */
export default function SakuraEffect({ count }: { count?: number }) {
  const sakura = useThemeStore((s) => s.sakura);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    if (!sakura || window.innerWidth < 768) return;
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!canvas || !ctx) return;

    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const pointer = { x: 0, y: 0, active: false, attract: false, moved: false };
    let width = window.innerWidth;
    let height = window.innerHeight;
    let primary = '';
    let primaryLight = '';
    let particles: DustParticle[] = [];
    let attractionQueue: DustParticle[] = [];
    let frame = 0;
    let lastTime = performance.now();

    const readColors = () => {
      const style = getComputedStyle(document.documentElement);
      primary = style.getPropertyValue('--primary').trim();
      primaryLight = style.getPropertyValue('--primary-light').trim();
    };

    const createParticle = (index: number, total: number): DustParticle => {
      const regionRadiusX = 48 + Math.random() * 42;
      const regionRadiusY = 36 + Math.random() * 30;
      const baseX = regionRadiusX + randomDustX(Math.random, dustRegionAt(index, total)) / 100 * Math.max(1, width - regionRadiusX * 2);
      const baseY = regionRadiusY + Math.random() * Math.max(1, height - regionRadiusY * 2);
      return {
        mode: 'ambient',
        baseX,
        baseY,
        regionRadiusX,
        regionRadiusY,
        x: baseX,
        y: baseY,
        pull: 0,
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
    };

    const draw = (time: number) => {
      ctx.clearRect(0, 0, width, height);
      // 连线放在粒子下层，透明度随牵引状态同步变化
      ctx.strokeStyle = primary;
      ctx.lineCap = 'round';
      const linkedParticles = particles.filter((p) => p.mode === 'attracted' && p.pull >= 0.015);
      ctx.lineWidth = 0.55;
      for (let i = 0; i < linkedParticles.length; i++) {
        const a = linkedParticles[i];
        for (let j = i + 1; j < linkedParticles.length; j++) {
          const b = linkedParticles[j];
          const distance = Math.hypot(a.x - b.x, a.y - b.y);
          if (distance >= PARTICLE_LINK_DISTANCE) continue;
          ctx.globalAlpha = (1 - distance / PARTICLE_LINK_DISTANCE) * a.pull * b.pull * 0.26;
          ctx.beginPath();
          ctx.moveTo(a.x, a.y);
          ctx.lineTo(b.x, b.y);
          ctx.stroke();
        }
      }
      ctx.lineWidth = 0.7;
      for (const p of particles) {
        if (p.mode === 'ambient' || p.pull < 0.015) continue;
        ctx.globalAlpha = p.pull * 0.32;
        ctx.beginPath();
        ctx.moveTo(pointer.x, pointer.y);
        ctx.lineTo(p.x, p.y);
        ctx.stroke();
      }
      ctx.globalAlpha = 1;
      ctx.fillStyle = primary;
      ctx.shadowColor = primaryLight;
      ctx.shadowBlur = 7;
      for (const p of particles) {
        const pulse = 0.5 + 0.5 * Math.sin(time * 0.8 + p.phase);
        ctx.globalAlpha = Math.min(0.85, (0.18 + (p.maxAlpha - 0.18) * pulse) * (1 + p.pull * 0.15));
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.radius * (1 + p.pull * 0.12), 0, TAU);
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
      attractionQueue = [];
      const particleCount = count ?? getDynamicParticleCount(width, height);
      particles = width < 768 ? [] : Array.from({ length: particleCount }, (_, index) => createParticle(index, particleCount));
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
      pointer.moved = true;
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
      if (pointer.active) updatePointerMode(document.elementFromPoint(pointer.x, pointer.y));
    };

    const release = (p: DustParticle) => {
      if (p.mode === 'attracted') p.mode = 'ambient';
    };
    const releaseAll = () => {
      attractionQueue.forEach(release);
      attractionQueue = [];
    };
    const updateAttractionQueue = () => {
      // 鼠标移动时，从当前牵引粒子与环境粒子中重新选出最近的 16 个
      const candidates = [
        ...attractionQueue,
        ...particles.filter((p) => p.mode === 'ambient'),
      ];
      const nearest = candidates
        .sort((a, b) => Math.hypot(a.x - pointer.x, a.y - pointer.y)
          - Math.hypot(b.x - pointer.x, b.y - pointer.y))
        .slice(0, ATTRACTED_COUNT);
      const selected = new Set(nearest);
      attractionQueue.forEach((p) => {
        if (!selected.has(p)) release(p);
      });
      nearest.forEach((p) => { p.mode = 'attracted'; });
      attractionQueue = nearest;
    };

    const applyRepulsion = (step: number) => {
      for (let i = 0; i < attractionQueue.length; i++) {
        const a = attractionQueue[i];
        for (let j = i + 1; j < attractionQueue.length; j++) {
          const b = attractionQueue[j];
          const dx = b.x - a.x;
          const dy = b.y - a.y;
          const distance = Math.hypot(dx, dy);
          if (distance >= PARTICLE_REPULSION_RADIUS) continue;
          const nx = distance > 0.001 ? dx / distance : Math.cos(a.phase);
          const ny = distance > 0.001 ? dy / distance : Math.sin(a.phase);
          const force = (1 - distance / PARTICLE_REPULSION_RADIUS)
            * PARTICLE_REPULSION_STRENGTH * Math.min(a.pull, b.pull) * step;
          a.x -= nx * force;
          a.y -= ny * force;
          b.x += nx * force;
          b.y += ny * force;
        }
      }
    };

    /** 所有运动最终都约束在粒子自己的固定椭圆区域内 */
    const constrainToRegion = (p: DustParticle) => {
      const dx = p.x - p.baseX;
      const dy = p.y - p.baseY;
      const normalized = Math.hypot(dx / p.regionRadiusX, dy / p.regionRadiusY);
      if (normalized <= 1) return;
      p.x = p.baseX + dx / normalized;
      p.y = p.baseY + dy / normalized;
    };

    const animate = (now: number) => {
      const dt = Math.min((now - lastTime) / 1000, 0.05);
      const step = Math.min(dt * 60, 2);
      const time = now / 1000;
      lastTime = now;
      if (pointer.attract) {
        if (pointer.moved || attractionQueue.length === 0) updateAttractionQueue();
      } else if (attractionQueue.length) releaseAll();
      pointer.moved = false;

      for (const p of particles) {
        const targetPull = p.mode === 'attracted' ? 1 : 0;
        p.pull += (targetPull - p.pull) * (1 - Math.pow(0.9, step));
        p.orbitAngle += p.orbitSpeed * dt * (0.25 + p.pull);
        const ambientX = p.baseX + Math.sin(time * p.driftSpeed + p.phase) * p.driftX;
        const ambientY = p.baseY + Math.cos(time * p.driftSpeed * 0.8 + p.phase) * p.driftY;
        const orbitX = pointer.x + Math.cos(p.orbitAngle) * p.orbitRadius;
        const orbitY = pointer.y + Math.sin(p.orbitAngle) * p.orbitRadius * 0.68;
        const targetX = ambientX + (orbitX - ambientX) * p.pull;
        const targetY = ambientY + (orbitY - ambientY) * p.pull;
        const ease = 1 - Math.pow(1 - (0.018 + p.pull * 0.065), step);
        p.x += (targetX - p.x) * ease;
        p.y += (targetY - p.y) * ease;
      }

      applyRepulsion(step);
      particles.forEach(constrainToRegion);
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
