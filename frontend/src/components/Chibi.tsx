/**
 * 迷你卡通（Q 版）角色素材组件。
 * 素材来源（均为官方 Q 版表情/贴纸，已下载到 public/chibi 本地）：
 * - 原神：Paimon Paintings 官方表情集（派蒙/胡桃/纳西妲/芙宁娜/可莉/雷电将军/钟离/温迪/八重神子/神里绫华/魈/甘雨）
 * - 崩坏：星穹铁道：官方贴纸（三月七/黑塔/白露/流萤/银狼/停云/克拉拉/卡芙卡）
 * 素材仅作个人博客非商业装饰用途。
 */

export interface ChibiMeta {
  src: string;
  /** 角色名 */
  name: string;
  /** 出处 */
  game: '原神' | '崩铁';
}

/** 全部可用的 Q 版角色素材 */
export const CHIBI_LIST: Record<string, ChibiMeta> = {
  paimon: { src: '/chibi/paimon.png', name: '派蒙', game: '原神' },
  hutao: { src: '/chibi/hutao.png', name: '胡桃', game: '原神' },
  nahida: { src: '/chibi/nahida.png', name: '纳西妲', game: '原神' },
  furina: { src: '/chibi/furina.png', name: '芙宁娜', game: '原神' },
  klee: { src: '/chibi/klee.png', name: '可莉', game: '原神' },
  raiden: { src: '/chibi/raiden.png', name: '雷电将军', game: '原神' },
  zhongli: { src: '/chibi/zhongli.png', name: '钟离', game: '原神' },
  venti: { src: '/chibi/venti.png', name: '温迪', game: '原神' },
  yae: { src: '/chibi/yae.png', name: '八重神子', game: '原神' },
  ayaka: { src: '/chibi/ayaka.png', name: '神里绫华', game: '原神' },
  xiao: { src: '/chibi/xiao.png', name: '魈', game: '原神' },
  ganyu: { src: '/chibi/ganyu.png', name: '甘雨', game: '原神' },
  march7: { src: '/chibi/march7.png', name: '三月七', game: '崩铁' },
  herta: { src: '/chibi/herta.png', name: '黑塔', game: '崩铁' },
  bailu: { src: '/chibi/bailu.png', name: '白露', game: '崩铁' },
  firefly: { src: '/chibi/firefly.png', name: '流萤', game: '崩铁' },
  silverwolf: { src: '/chibi/silverwolf.png', name: '银狼', game: '崩铁' },
  tingyun: { src: '/chibi/tingyun.png', name: '停云', game: '崩铁' },
  clara: { src: '/chibi/clara.png', name: '克拉拉', game: '崩铁' },
  kafka: { src: '/chibi/kafka.png', name: '卡芙卡', game: '崩铁' },
};

interface ChibiImageProps {
  /** CHIBI_LIST 中的键名 */
  name: keyof typeof CHIBI_LIST;
  size?: number;
  className?: string;
  style?: React.CSSProperties;
  /** 是否显示角色名牌 */
  showName?: boolean;
}

/** Q 版角色图片（带轻微上下浮动动画可通过 style/className 附加） */
export function ChibiImage({ name, size = 96, className, style, showName }: ChibiImageProps) {
  const meta = CHIBI_LIST[name];
  if (!meta) return null;
  const img = (
    <img
      src={meta.src}
      alt={meta.name}
      title={`${meta.name}（${meta.game}）`}
      width={size}
      height={size}
      loading="lazy"
      style={{ objectFit: 'contain', display: 'block', ...style }}
      className={className}
      draggable={false}
    />
  );
  if (!showName) return img;
  return (
    <span style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'center', gap: 2 }} className={className}>
      {img}
      <span style={{ fontSize: 12, color: 'var(--text-faint)' }}>{meta.name}</span>
    </span>
  );
}
