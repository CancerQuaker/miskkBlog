import { useEffect, useRef } from 'react';
import { useThemeStore } from '../stores/themeStore';

type ParticleMode = 'ambient' | 'attracted' | 'retiring';

interface DustParticle {
  mode: ParticleMode;
  baseX: number;
  baseY: number;
  x: number;
  y: number;
  pull: number;
  fade: number;
  availableAt: number;
  radius: number;
  maxAlpha: number;
  phase: number;
  driftSpeed: number;
  driftX: number;
  driftY: number;
  orbitAngle: number;
  orbitSpeed: number;
}

const TAU = Math.PI * 2;
const ATTRACTED_COUNT = 16;
const MOUSE_INNER_RADIUS = 60;
const MOUSE_OUTER_RADIUS = 200;
const PARTICLE_LINK_DISTANCE = 110;
const PARTICLE_REPULSION_RADIUS = 36;
const PARTICLE_REPULSION_STRENGTH = 0.65;
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

/** Canvas 柔光尘：固定数量牵引，原区域自动补充，卡片区域始终随机漂浮 */
export default function SakuraEffect({ count = 128 }: { count?: number }) {
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
    let lastScrollY = window.scrollY;
    let ringVisibility = 0;

    const readColors = () => {
      const style = getComputedStyle(document.documentElement);
      primary = style.getPropertyValue('--primary').trim();
      primaryLight = style.getPropertyValue('--primary-light').trim();
    };

    const createParticle = (
      baseX = randomDustX(Math.random) / 100 * width,
      baseY = Math.random() * height,
      fade = 1,
    ): DustParticle => ({
      mode: 'ambient',
      baseX,
      baseY,
      x: baseX,
      y: baseY,
      pull: 0,
      fade,
      availableAt: 0,
      radius: 1 + Math.random() * 1.5,
      maxAlpha: 0.45 + Math.random() * 0.25,
      phase: Math.random() * TAU,
      driftSpeed: 0.2 + Math.random() * 0.35,
      driftX: 8 + Math.random() * 16,
      driftY: 10 + Math.random() * 20,
      orbitAngle: Math.random() * TAU,
      orbitSpeed: (0.25 + Math.random() * 0.5) * (Math.random() < 0.5 ? -1 : 1),
    });

    const draw = (time: number) => {
      ctx.clearRect(0, 0, width, height);
      if (ringVisibility > 0.01) {
        ctx.strokeStyle = primary;
        ctx.lineWidth = 0.8;
        ctx.setLineDash([5, 7]);
        ctx.globalAlpha = ringVisibility * 0.24;
        ctx.beginPath();
        ctx.arc(pointer.x, pointer.y, MOUSE_OUTER_RADIUS, 0, TAU);
        ctx.stroke();
        ctx.setLineDash([2, 4]);
        ctx.globalAlpha = ringVisibility * 0.38;
        ctx.beginPath();
        ctx.arc(pointer.x, pointer.y, MOUSE_INNER_RADIUS, 0, TAU);
        ctx.stroke();
        ctx.setLineDash([]);
        ctx.globalAlpha = 1;
      }
      // 连线放在粒子下层，透明度随牵引和退出状态同步变化
      ctx.strokeStyle = primary;
      ctx.lineCap = 'round';
      const linkedParticles = particles.filter((p) => p.mode !== 'ambient' && p.pull >= 0.015);
      ctx.lineWidth = 0.55;
      for (let i = 0; i < linkedParticles.length; i++) {
        const a = linkedParticles[i];
        for (let j = i + 1; j < linkedParticles.length; j++) {
          const b = linkedParticles[j];
          const distance = Math.hypot(a.x - b.x, a.y - b.y);
          if (distance >= PARTICLE_LINK_DISTANCE) continue;
          ctx.globalAlpha = (1 - distance / PARTICLE_LINK_DISTANCE) * a.pull * a.fade * b.pull * b.fade * 0.26;
          ctx.beginPath();
          ctx.moveTo(a.x, a.y);
          ctx.lineTo(b.x, b.y);
          ctx.stroke();
        }
      }
      ctx.lineWidth = 0.7;
      for (const p of particles) {
        const distance = Math.hypot(p.x - pointer.x, p.y - pointer.y);
        const inAttractionBand = distance >= MOUSE_INNER_RADIUS && distance <= MOUSE_OUTER_RADIUS;
        if (p.mode === 'ambient' || p.pull < 0.015
          || (p.mode === 'attracted' && !inAttractionBand)) continue;
        ctx.globalAlpha = p.pull * p.fade * 0.32;
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
        const alpha = Math.min(0.85, (0.18 + (p.maxAlpha - 0.18) * pulse) * (1 + p.pull * 0.15));
        ctx.globalAlpha = alpha * Math.max(0, p.fade);
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
      particles = width < 768 ? [] : Array.from({ length: count }, () => createParticle());
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
      const delta = window.scrollY - lastScrollY;
      lastScrollY = window.scrollY;
      for (const p of particles) {
        if (p.mode !== 'ambient') continue;
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

    const retire = (p: DustParticle) => {
      if (p.mode === 'attracted') p.mode = 'retiring';
    };
    const releaseAll = () => {
      attractionQueue.forEach(retire);
      attractionQueue = [];
    };
    const replenish = (p: DustParticle, now: number) => {
      const replacement = createParticle(p.baseX, p.baseY, 0);
      replacement.availableAt = now + 800;
      particles.push(replacement);
    };
    const attract = (p: DustParticle, now: number) => {
      p.mode = 'attracted';
      replenish(p, now);
    };
    const updateAttractionQueue = (now: number) => {
      // 鼠标移动时，从当前牵引粒子与环境粒子中重新选出最近的 16 个
      const candidates = [
        ...attractionQueue,
        ...particles.filter((p) => p.mode === 'ambient' && p.availableAt <= now),
      ];
      const nearest = candidates
        .sort((a, b) => Math.hypot(a.x - pointer.x, a.y - pointer.y)
          - Math.hypot(b.x - pointer.x, b.y - pointer.y))
        .slice(0, ATTRACTED_COUNT);
      const selected = new Set(nearest);
      attractionQueue.forEach((p) => {
        if (!selected.has(p)) retire(p);
      });
      nearest.forEach((p) => {
        if (p.mode === 'ambient') attract(p, now);
      });
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

    const animate = (now: number) => {
      const dt = Math.min((now - lastTime) / 1000, 0.05);
      const step = Math.min(dt * 60, 2);
      const time = now / 1000;
      lastTime = now;
      if (pointer.attract) {
        if (pointer.moved || attractionQueue.length === 0) updateAttractionQueue(now);
      } else if (attractionQueue.length) releaseAll();
      pointer.moved = false;
      ringVisibility += ((pointer.attract ? 1 : 0) - ringVisibility) * (1 - Math.pow(0.9, step));

      for (const p of particles) {
        if (p.mode === 'retiring') {
          p.pull += (0 - p.pull) * (1 - Math.pow(0.88, step));
          p.fade = Math.max(0, p.fade - dt * 3);
          continue;
        }
        if (p.mode === 'ambient') p.fade = Math.min(1, p.fade + dt * 1.8);
        p.orbitAngle += p.orbitSpeed * dt * (0.25 + p.pull);
        const ambientX = p.baseX + Math.sin(time * p.driftSpeed + p.phase) * p.driftX;
        const ambientY = p.baseY + Math.cos(time * p.driftSpeed * 0.8 + p.phase) * p.driftY;
        let targetX = ambientX;
        let targetY = ambientY;
        let targetPull = 0;
        if (p.mode === 'attracted') {
          const dx = p.x - pointer.x;
          const dy = p.y - pointer.y;
          const distance = Math.max(Math.hypot(dx, dy), 0.001);
          if (distance < MOUSE_INNER_RADIUS) {
            // 内圈互斥：沿当前方向把粒子推到内圈之外
            targetPull = 1;
            const targetRadius = MOUSE_INNER_RADIUS + 48;
            targetX = pointer.x + dx / distance * targetRadius;
            targetY = pointer.y + dy / distance * targetRadius;
            const repelForce = (1 - distance / MOUSE_INNER_RADIUS) * 2.2 * step;
            p.x += dx / distance * repelForce;
            p.y += dy / distance * repelForce;
          } else if (distance <= MOUSE_OUTER_RADIUS) {
            // 环带牵引：粒子缓慢收拢到内圈边缘，并保留切向运动
            targetPull = 1;
            const targetRadius = MOUSE_INNER_RADIUS + 36;
            targetX = pointer.x + Math.cos(p.orbitAngle) * targetRadius;
            targetY = pointer.y + Math.sin(p.orbitAngle) * targetRadius;
          }
        }
        p.pull += (targetPull - p.pull) * (1 - Math.pow(0.9, step));
        targetX = ambientX + (targetX - ambientX) * p.pull;
        targetY = ambientY + (targetY - ambientY) * p.pull;
        const ease = 1 - Math.pow(1 - (0.018 + p.pull * 0.065), step);
        p.x += (targetX - p.x) * ease;
        p.y += (targetY - p.y) * ease;
      }

      applyRepulsion(step);
      particles = particles.filter((p) => p.mode !== 'retiring' || p.fade > 0);
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
