import { useEffect } from 'react';
import { loadOml2d } from 'oh-my-live2d';
import { live2dConfig } from '../config/site';
import { useThemeStore } from '../stores/themeStore';

let instance: ReturnType<typeof loadOml2d> | null = null;

/** 看板娘舞台尺寸（与下方 loadOml2d 的 stageStyle 保持一致，拖动时用于边界计算） */
const STAGE_SIZE = { width: 245, height: 220 };
/** 看板娘拖动后位置的持久化键：存 { left, top }，单位 px */
const LIVE2D_POSITION_KEY = 'miskk-live2d-position';
/** 累计位移超过该像素阈值才判定为拖动，否则视为点击（放行模型命中区动作） */
const DRAG_THRESHOLD = 4;

/** 读取持久化的看板娘位置；视口变小或数值异常时返回 null，回退默认右下角 */
function loadSavedPosition(): { left: number; top: number } | null {
  try {
    const raw = localStorage.getItem(LIVE2D_POSITION_KEY);
    if (!raw) return null;
    const pos = JSON.parse(raw);
    if (
      typeof pos?.left !== 'number' || typeof pos?.top !== 'number' ||
      !Number.isFinite(pos.left) || !Number.isFinite(pos.top)
    ) return null;
    if (
      pos.left < 0 || pos.top < 0 ||
      pos.left + STAGE_SIZE.width > window.innerWidth + 2 ||
      pos.top + STAGE_SIZE.height > window.innerHeight + 2
    ) return null;
    return { left: pos.left, top: pos.top };
  } catch {
    return null;
  }
}

/**
 * 右下角 Live2D 看板娘（oh-my-live2d），模型视线自动跟随鼠标，
 * 闲置时轮播可爱语录；移动端（窄屏/触摸设备）不加载，外观面板可开关
 * （库未提供 destroy，关闭时通过 html.live2d-off 类隐藏根元素）。
 * 「返回顶部」按钮已移到左下角，二者互不遮挡。
 * 交互：单击头部→睡觉，双击头部→唱歌，点击身体→Anim 动作（需模型文件支持对应的 hit areas）；
 * 按住拖动可把看板娘挪到任意位置，位置持久化到 localStorage。
 *
 * 拖动实现说明：oh-my-live2d 0.19 的 #oml2d-stage 定位样式在每次模型加载后由
 * stage.reloadStyle() 整体重设（会把外部的 left/top/right 全部覆盖回默认），因此不能
 * 只设置一次坐标——用 MutationObserver 监听 stage 的 style 属性，在库每次重设后补回
 * 自定义坐标；拖拽通过捕获阶段监听 pointerdown 与库内 PIXI 命中区共存，位移小于阈值
 * 时不拦截，点击/双击动作不受影响。
 */
export default function Live2DKanban() {
  const live2d = useThemeStore((s) => s.live2d);

  useEffect(() => {
    document.documentElement.classList.toggle('live2d-off', !live2d);
  }, [live2d]);

  useEffect(() => {
    if (!live2d) return;
    // 移动端（窄屏或触摸设备）不加载看板娘，避免遮挡内容与流量消耗
    const narrow = window.matchMedia('(max-width: 768px)').matches;
    const touch = window.matchMedia('(pointer: coarse)').matches;
    if (narrow || touch) return;

    const dayMsg = live2dConfig.welcome[0];
    const nightMsg = live2dConfig.welcome[1];

    // 库无 destroy API，实例只加载一次；但拖拽监听必须在每次 effect 执行时重挂——
    // StrictMode 开发模式会「执行 → 清理 → 再执行」，若把 enableDrag 放在加载分支内，
    // 第二次执行会因 instance 已存在而跳过，拖拽监听被清理后再也挂不回来
    if (!instance) instance = loadOml2d({
      dockedPosition: 'right',
      // 状态条（加载/切换模型提示）也跟随主题色；var() 在内联样式中同样生效
      primaryColor: 'var(--primary)',
      menus: {
        disable: true,
      },
      models: [
        {
          path: live2dConfig.modelUrl,
          // 想再调大小只改这里：scale 按比例缩放模型，stageStyle 是舞台（画布）尺寸
          position: [0, 30],
          scale: 0.078,
          stageStyle: {
            height: 220,
            width: 245,
          },
          mobileScale: 0.078,
        },
      ],
      tips: {
        // 气泡底色/边框跟随当前主题的卡片样式（--bg-card-transparent / --border），
        // 文字颜色用 --primary（外观面板自选主题色时经 main.tsx 行内覆盖同步变化）；
        // CSS 变量在内联样式中实时解析，整体覆盖库默认的蓝底白字
        style: {
          backgroundColor: 'var(--bg-card-transparent)',
          border: '1px solid var(--border)',
          color: 'var(--primary)',
          boxShadow: 'var(--shadow-card)',
          filter: 'none',
          backdropFilter: 'blur(8px)',
          WebkitBackdropFilter: 'blur(8px)',
        },
        idleTips: {
          message: live2dConfig.tips,
          interval: 12000,
        },
        welcomeTips: {
          message: {
            daybreak: nightMsg,
            morning: dayMsg,
            noon: dayMsg,
            afternoon: dayMsg,
            dusk: dayMsg,
            night: nightMsg,
            lateNight: nightMsg,
            weeHours: nightMsg,
          },
        },
        copyTips: {
          message: ['复制成功嗷~ 记得注明出处哦！'],
        },
      },
    });

    enableDrag();

    return () => {
      disableDrag();
    };
  }, [live2d]);

  return null;
}

type DragState = {
  pointerId: number | null;
  startX: number;
  startY: number;
  left: number;
  top: number;
  moved: boolean;
};

const drag: DragState = {
  pointerId: null,
  startX: 0,
  startY: 0,
  left: 0,
  top: 0,
  moved: false,
};

let styleObserver: MutationObserver | null = null;

/**
 * 捕获阶段监听舞台 pointerdown：先于同元素上 PIXI 命中区（冒泡阶段）触发，
 * 开始跟踪按住手势；真正位移超阈值后才开始改坐标并拦截点击。
 */
const onPointerDownCapture = (e: PointerEvent) => {
  if (e.button !== 0 || e.pointerType === 'touch') return;
  const stage = document.getElementById('oml2d-stage');
  if (!stage) return;
  const rect = stage.getBoundingClientRect();
  drag.pointerId = e.pointerId;
  drag.startX = e.clientX;
  drag.startY = e.clientY;
  drag.left = rect.left;
  drag.top = rect.top;
  drag.moved = false;
  window.addEventListener('pointermove', onPointerMove);
  window.addEventListener('pointerup', onPointerUp);
  window.addEventListener('pointercancel', onPointerUp);
};

const onPointerMove = (e: PointerEvent) => {
  if (drag.pointerId === null || e.pointerId !== drag.pointerId) return;
  const dx = e.clientX - drag.startX;
  const dy = e.clientY - drag.startY;
  if (!drag.moved && Math.hypot(dx, dy) < DRAG_THRESHOLD) return;
  drag.moved = true;
  const maxLeft = Math.max(0, window.innerWidth - STAGE_SIZE.width);
  const maxTop = Math.max(0, window.innerHeight - STAGE_SIZE.height);
  applyPosition(
    Math.min(Math.max(drag.left + dx, 0), maxLeft),
    Math.min(Math.max(drag.top + dy, 0), maxTop),
  );
};

const onPointerUp = (e: PointerEvent) => {
  if (drag.pointerId === null || e.pointerId !== drag.pointerId) return;
  window.removeEventListener('pointermove', onPointerMove);
  window.removeEventListener('pointerup', onPointerUp);
  window.removeEventListener('pointercancel', onPointerUp);
  drag.pointerId = null;
  if (drag.moved) savePosition();
};

/** 把左上角坐标写到舞台上：清掉库设置的 right/bottom（避免与 left/top 同时生效） */
function applyPosition(left: number, top: number) {
  const stage = document.getElementById('oml2d-stage');
  if (!stage) return;
  // 拖动中禁用过渡保证跟手；恢复/补回坐标时移除，保留库的过渡效果
  if (drag.pointerId !== null) stage.style.setProperty('transition', 'none');
  else stage.style.removeProperty('transition');
  stage.style.setProperty('right', 'auto', 'important');
  stage.style.setProperty('bottom', 'auto');
  stage.style.setProperty('left', `${Math.round(left)}px`);
  stage.style.setProperty('top', `${Math.round(top)}px`);
}

function savePosition() {
  const stage = document.getElementById('oml2d-stage');
  if (!stage) return;
  localStorage.setItem(LIVE2D_POSITION_KEY, JSON.stringify({
    left: stage.offsetLeft,
    top: stage.offsetTop,
  }));
}

/** 恢复持久化位置（仅在舞台已挂载后调用） */
function restorePosition() {
  const saved = loadSavedPosition();
  if (!saved) return;
  applyPosition(saved.left, saved.top);
}

/**
 * 给 #oml2d-stage 挂拖拽。必须在 loadOml2d() 之后调用：
 * 库构造函数同步完成元素创建与挂载，此时 stage 已在 DOM 中。
 */
function enableDrag() {
  const stage = document.getElementById('oml2d-stage');
  if (!stage) return;

  // 库每次模型加载后 stage.reloadStyle() 会整体重设定位样式（Object.assign 逐属性覆盖，
  // left/top 会被顶回默认值），监听 style 属性变化，在重设后补回自定义坐标。
  // 拖动中的样式写入来自自身，直接跳过；位置已一致时也跳过，避免观察者自触发循环。
  styleObserver = new MutationObserver(() => {
    if (drag.pointerId !== null) return;
    const saved = loadSavedPosition();
    if (!saved) return;
    if (Math.abs(stage.offsetLeft - saved.left) < 1 && Math.abs(stage.offsetTop - saved.top) < 1) return;
    applyPosition(saved.left, saved.top);
  });
  styleObserver.observe(stage, { attributes: true, attributeFilter: ['style'] });

  stage.addEventListener('pointerdown', onPointerDownCapture, true);
  // 模型加载 + 舞台滑入动画完成后恢复位置，避免与库的初始 translateY 动画打架
  instance?.onStageSlideIn(() => restorePosition());
  window.addEventListener('resize', onWindowResize);
}

/** 视口变化时把看板娘拉回可见范围（仅在用户拖动过、存在持久化位置时介入） */
const onWindowResize = () => {
  if (!localStorage.getItem(LIVE2D_POSITION_KEY)) return;
  const stage = document.getElementById('oml2d-stage');
  if (!stage) return;
  const maxLeft = Math.max(0, window.innerWidth - STAGE_SIZE.width);
  const maxTop = Math.max(0, window.innerHeight - STAGE_SIZE.height);
  applyPosition(
    Math.min(Math.max(stage.offsetLeft, 0), maxLeft),
    Math.min(Math.max(stage.offsetTop, 0), maxTop),
  );
  savePosition();
};

function disableDrag() {
  styleObserver?.disconnect();
  styleObserver = null;
  window.removeEventListener('pointermove', onPointerMove);
  window.removeEventListener('pointerup', onPointerUp);
  window.removeEventListener('pointercancel', onPointerUp);
  window.removeEventListener('resize', onWindowResize);
  drag.pointerId = null;
  document.getElementById('oml2d-stage')?.removeEventListener('pointerdown', onPointerDownCapture, true);
}
