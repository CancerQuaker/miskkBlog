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

/** 音乐播放列表（换成你自己的 mp3 直链即可） */
export const musicConfig = {
  volume: 0.6,
  playlist: [
    { name: 'SoundHelix Song 1', artist: 'T. Schürger', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3' },
    { name: 'SoundHelix Song 2', artist: 'T. Schürger', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3' },
    { name: 'SoundHelix Song 3', artist: 'T. Schürger', url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3' },
  ],
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
