import { useEffect } from 'react';
import { loadOml2d } from 'oh-my-live2d';
import { live2dConfig } from '../config/site';
import { useThemeStore } from '../stores/themeStore';

let instance: ReturnType<typeof loadOml2d> | null = null;

/**
 * 右下角 Live2D 看板娘（oh-my-live2d），模型视线自动跟随鼠标，
 * 闲置时轮播可爱语录；移动端（窄屏/触摸设备）不加载，外观面板可开关
 * （库未提供 destroy，关闭时通过 html.live2d-off 类隐藏根元素）。
 * 「返回顶部」按钮已移到左下角，二者互不遮挡。
 * 交互：单击头部→睡觉，双击头部→唱歌，点击身体→Anim 动作（需模型文件支持对应的 hit areas）。
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
    if (narrow || touch || instance) return;

    const hour = new Date().getHours();
    const dayMsg = live2dConfig.welcome[0];
    const nightMsg = live2dConfig.welcome[1];

    instance = loadOml2d({
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
  }, [live2d]);

  return null;
}
