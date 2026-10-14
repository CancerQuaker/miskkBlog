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
      menus: {
        disable: true,
      },
      models: [
        {
          path: live2dConfig.modelUrl,
          position: [0, 30],
          scale: 0.068,
          stageStyle: {
            height: 200,
            width: 230,
          },
        },
      ],
      tips: {
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
