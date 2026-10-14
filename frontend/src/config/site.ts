/**
 * 站点静态配置：站名、关于页、图库、音乐播放列表、Live2D 等。
 */
export const siteConfig = {
  name: '迷失客客の博客',
  subtitle: '唔姆~ 这里是迷失客客的小窝，记录代码与日常捏！',
  /** 建站年份，用于页脚 */
  startYear: 2026,
  /** 站点地址（友链申请注意事项里展示用） */
  url: 'https://blog.miskk.cn',
  /** GitHub 仓库地址 */
  repo: 'https://github.com/miskk/miskkBlog',
};

export const aboutConfig = {
  author: {
    name: '迷失客客',
    avatarText: '客',
    bio: '一只喜欢写代码的小狐狸 | 后端 / 前端 / 摸鱼',
    skills: ['Java', 'Spring Boot', 'React', 'TypeScript', 'MySQL', 'Redis', 'Nacos'],
    socials: {
      github: 'https://github.com/miskk',
      bilibili: 'https://space.bilibili.com',
      email: 'admin@miskk.cn',
    },
  },
  project: {
    intro: [
      '本项目是一个完全开源的二次元风格博客系统，前端基于 Vite + React 18 + Ant Design 5，后端采用 Spring Boot 3.3 微服务架构（Spring Cloud Alibaba + Nacos 注册中心），数据层为 MySQL + MyBatis-Plus，缓存使用 Redis，接口文档由 Knife4j 提供。',
      '站点特色：魔法星星鼠标指针（星星拖尾、左键掉落富强/民主/文明/和谐等字符）、自定义右键菜单（返回/复制/另存/检查）、小狐狸 Live2D 看板娘（仙狐小姐）、纳西妲主题首页横幅、蓝紫梦幻主题、樱花飘落特效、亮/暗/护眼三主题、文章评论点赞分享、友链申请审核、ECharts 数据看板与系统资源监控。',
      '后端由 5 个服务组成：网关（鉴权/路由）、用户服务、文章服务、友链服务、管理服务，服务间通过 OpenFeign 通信，浏览量等高频写入经 Redis 缓冲后批量落库。',
    ],
    techs: [
      { label: '前端', value: 'Vite 6 / React 18 / TS / AntD 5 / ECharts' },
      { label: '后端', value: 'Spring Boot 3.3 / Spring Cloud / Nacos / OpenFeign' },
      { label: '数据', value: 'MySQL 8 / Redis / MyBatis-Plus' },
      { label: '文档', value: 'Knife4j (OpenAPI 3)' },
    ],
  },
};

/** 「我的」页面图库（可替换为任意图片地址） */
export const galleryConfig = [
  { src: 'https://picsum.photos/seed/miskk1/800/600', caption: '夕阳下的小狐狸' },
  { src: 'https://picsum.photos/seed/miskk2/600/800', caption: '春日祭' },
  { src: 'https://picsum.photos/seed/miskk3/800/500', caption: '桌面上的新键盘' },
  { src: 'https://picsum.photos/seed/miskk4/700/700', caption: '周末的咖啡店' },
  { src: 'https://picsum.photos/seed/miskk5/800/450', caption: '深夜写代码' },
  { src: 'https://picsum.photos/seed/miskk6/600/750', caption: '在海边' },
  { src: 'https://picsum.photos/seed/miskk7/800/600', caption: '秋叶原之旅' },
  { src: 'https://picsum.photos/seed/miskk8/750/700', caption: '新买的痛包' },
  { src: 'https://picsum.photos/seed/miskk9/800/550', caption: '冬天的暖炉' },
];

/** 歌曲条目（meting 拉取结果与本地歌单共用） */
export interface MusicTrack {
  name: string;
  artist: string;
  url: string;
  cover?: string;
  lrc?: string;
}

/**
 * 音乐播放器配置（参考 cuteleaf.cn / Firefly 主题的实现）：
 * - mode 'meting'：通过 Meting API 拉取网易云歌单（默认，与参考站同款目录）；
 *   主 API 失效时按 fallbackApis 顺序自动重试。
 * - mode 'local'：使用本地 mp3（放 frontend/public/music/，支持 cover 封面与 lrc 歌词）。
 * 公开 Meting 站点稳定性参差：injahow 目前全链路可用（音频 302 到网易 CDN），moeyao 音频正常但歌词/封面有杂讯，i-meto 歌词正常但音频代理已挂。
 */
export const musicConfig = {
  mode: 'meting' as 'meting' | 'local',
  /** 默认音量（0-1），仅首次访问生效，之后浏览器记住上次调节 */
  volume: 0.6,
  /** 默认播放模式：list 列表循环 / one 单曲循环 / random 随机播放 */
  playMode: 'list' as 'list' | 'one' | 'random',
  /** 播放出错自动切下一首（连续失败超过曲单长度则停下提示） */
  autoSkipOnError: true,
  meting: {
    api: 'https://api.injahow.cn/meting/?server=:server&type=:type&id=:id',
    fallbackApis: [
      'https://api.moeyao.cn/meting/?server=:server&type=:type&id=:id',
      'https://api.i-meto.com/meting/api?server=:server&type=:type&id=:id&r=:r',
    ],
    server: 'netease',
    type: 'playlist',
    /** 网易云歌单：知更鸟《空气中的听觉幻术》单曲+伴奏（与 cuteleaf.cn 参考站相同目录） */
    id: '10046455237',
  },
  /** 本地歌单（mode 为 local 时使用） */
  local: {
    playlist: [
      {
        name: '使一颗心免于哀伤（哼唱版）',
        artist: '知更鸟 / HOYO-MiX / Chevy',
        url: '/music/使一颗心免于哀伤-哼唱.mp3',
        cover: '/music/cover/109951169585655912.webp',
      },
    ] as MusicTrack[],
  },
};

/** Live2D 看板娘配置（oh-my-live2d）：狐狸娘「仙狐小姐」（贤惠幼妻仙狐小姐），模型文件放在本地 public/live2d 下 */
export const live2dConfig = {
  modelUrl: '/live2d/senko/senko.model3.json',
  tips: [
    '嗷呜~ 欢迎回来！',
    '小狐狸的眼睛会跟着鼠标转哦~',
    '累了就来摸摸小狐狸吧',
    '记得给喜欢的文章点赞嗷！',
    '右下角的小狐狸一直都在哦',
    '摸摸头~ 今天也要开心呀',
  ],
  welcome: ['哇！是主人回来了嗷~', '欢迎来到迷失客客の博客！小狐狸恭候多时~'],
};
