import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Dropdown, Input, Avatar, Drawer, Popover, Switch } from 'antd';
import {
  HomeOutlined,
  SearchOutlined,
  MenuOutlined,
  UserOutlined,
  LogoutOutlined,
  DashboardOutlined,
  BgColorsOutlined,
  DownOutlined,
  SunOutlined,
  MoonOutlined,
  EyeOutlined,
  LaptopOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '../stores/authStore';
import { useThemeStore, ThemeMode, ACCENT_PRESETS } from '../stores/themeStore';
import MusicPlayer from '../components/MusicPlayer';
import { siteConfig } from '../config/site';

/** 小窝 Logo：魔法四角星（与星星鼠标指针同款） */
export function CatLogo({ size = 34 }: { size?: number }) {
  return (
    <svg viewBox="0 0 64 64" width={size} height={size}>
      {/* 圆角星盘底座 */}
      <rect x="3" y="3" width="58" height="58" rx="18" fill="var(--primary-weak)" stroke="var(--primary)" strokeWidth="2.5" />
      {/* 主星：大四角星 */}
      <path d="M32 10 L37.2 26.8 L54 32 L37.2 37.2 L32 54 L26.8 37.2 L10 32 L26.8 26.8 Z" fill="var(--primary)" />
      {/* 星核 */}
      <circle cx="32" cy="32" r="4.2" fill="#fff" />
      {/* 环绕小星 */}
      <path d="M47.5 11 L49 15.4 L53.4 17 L49 18.6 L47.5 23 L46 18.6 L41.6 17 L46 15.4 Z" fill="var(--primary)" opacity=".75" />
      <path d="M16.5 41 L17.7 44.5 L21.2 45.7 L17.7 46.9 L16.5 50.4 L15.3 46.9 L11.8 45.7 L15.3 44.5 Z" fill="var(--primary)" opacity=".55" />
    </svg>
  );
}

const themeOptions: { key: ThemeMode; label: string }[] = [
  { key: 'light', label: '🌤 亮色' },
  { key: 'dark', label: '🌙 暗色' },
  { key: 'eye', label: '🌿 护眼' },
  { key: 'system', label: '💻 跟随系统' },
];

/** 亮度按钮图标跟随当前模式切换（跟随系统时显示显示器图标） */
const modeIcons: Record<ThemeMode, React.ReactNode> = {
  light: <SunOutlined style={{ fontSize: 18 }} />,
  dark: <MoonOutlined style={{ fontSize: 18 }} />,
  eye: <EyeOutlined style={{ fontSize: 18 }} />,
  system: <LaptopOutlined style={{ fontSize: 18 }} />,
};

/** 亮度面板：亮色 / 暗色 / 护眼 / 跟随系统 */
function BrightnessPanel() {
  const { mode, setMode } = useThemeStore();

  return (
    <div style={{ width: 232, padding: '4px 2px' }}>
      <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-faint)', marginBottom: 8 }}>亮度</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
        {themeOptions.map((o) => (
          <button
            key={o.key}
            className={`theme-chip ${mode === o.key ? 'active' : ''}`}
            onClick={() => setMode(o.key)}
          >
            {o.label}
          </button>
        ))}
      </div>
    </div>
  );
}

/** 外观面板：主题色 + 特效开关（飘落物 / 鼠标样式 / 看板娘） */
function AppearancePanel() {
  const { accent, setAccent, sakura, setSakura, starCursor, setStarCursor, live2d, setLive2d } =
    useThemeStore();

  return (
    <div style={{ width: 248, padding: '4px 2px' }}>
      <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-faint)', marginBottom: 8 }}>主题色</div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
        {ACCENT_PRESETS.map((p) => {
          const selected = p.color === accent || (p.color === null && accent === null);
          return (
            <span
              key={p.name}
              className={`accent-dot ${selected ? 'selected' : ''}`}
              title={p.name}
              style={p.color ? { background: p.color } : undefined}
              onClick={() => setAccent(p.color)}
            >
              {!p.color && <span className="accent-dot-default">默</span>}
            </span>
          );
        })}
        <label className="accent-dot accent-custom" title="自定义颜色">
          <input
            type="color"
            value={accent || '#80a942'}
            onChange={(e) => setAccent(e.target.value)}
          />
          <span className="accent-custom-plus">＋</span>
        </label>
      </div>
      {accent && accent.toLowerCase() !== '#80a942' && (
        <div style={{ marginTop: 10, fontSize: 12, color: 'var(--text-sub)' }}>
          当前：<span style={{ color: accent, fontWeight: 600 }}>{accent}</span>
          <span className="accent-reset" onClick={() => setAccent(null)}> 恢复默认</span>
        </div>
      )}

      <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-faint)', margin: '14px 0 4px' }}>特效</div>
      <div className="effect-row">
        <div className="effect-info">
          <div className="effect-label">🌸 飘落物</div>
          <div className="effect-desc">樱花花瓣缓缓飘落</div>
        </div>
        <Switch size="small" checked={sakura} onChange={setSakura} />
      </div>
      <div className="effect-row">
        <div className="effect-info">
          <div className="effect-label">✦ 鼠标样式</div>
          <div className="effect-desc">星星指针 + 点击蹦字</div>
        </div>
        <Switch size="small" checked={starCursor} onChange={setStarCursor} />
      </div>
      <div className="effect-row">
        <div className="effect-info">
          <div className="effect-label">🦊 看板娘</div>
          <div className="effect-desc">右下角 Live2D 小狐狸</div>
        </div>
        <Switch size="small" checked={live2d} onChange={setLive2d} />
      </div>
    </div>
  );
}

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();
  const mode = useThemeStore((s) => s.mode);
  const [logoHover, setLogoHover] = useState(false);
  const [search, setSearch] = useState('');
  const [musicOpen, setMusicOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const doSearch = (kw: string) => {
    if (!kw.trim()) return;
    navigate(`/search?q=${encodeURIComponent(kw.trim())}`);
    setMobileOpen(false);
  };

  const articleMenu = {
    items: [
      { key: 'all', label: <Link to="/articles">全部文章</Link> },
      { key: 'cat', label: <Link to="/articles?tab=category">分类</Link> },
      { key: 'tag', label: <Link to="/articles?tab=tag">标签</Link> },
    ],
  };

  const isActive = (path: string) =>
    path === '/' ? location.pathname === '/' : location.pathname.startsWith(path);

  const links = (
    <>
      <Link to="/" className={`nav-link ${isActive('/') ? 'active' : ''}`}>主页</Link>
      <Dropdown menu={articleMenu} trigger={['hover', 'click']}>
        <Link to="/articles" className={`nav-link ${isActive('/articles') || isActive('/article/') || isActive('/search') ? 'active' : ''}`}>
          文章 <DownOutlined style={{ fontSize: 10 }} />
        </Link>
      </Dropdown>
      <Link to="/links" className={`nav-link ${isActive('/links') ? 'active' : ''}`}>友链</Link>
      <Link to="/mine" className={`nav-link ${isActive('/mine') ? 'active' : ''}`}>我的</Link>
      <Link to="/about" className={`nav-link ${isActive('/about') ? 'active' : ''}`}>关于</Link>
    </>
  );

  return (
    <header className="navbar" onClick={() => musicOpen && setMusicOpen(false)}>
      <div className="navbar-inner">
        {/* Logo：悬停切换为「主页」图标 */}
        <Link
          to="/"
          className="nav-logo"
          onMouseEnter={() => setLogoHover(true)}
          onMouseLeave={() => setLogoHover(false)}
          title={logoHover ? '回到主页' : siteConfig.name}
        >
          <span className="logo-switch" style={{ opacity: logoHover ? 0 : 1, pointerEvents: logoHover ? 'none' : 'auto' }}>
            <CatLogo />
            <span>{siteConfig.name}</span>
          </span>
          <span className="logo-switch logo-switch-hover" style={{ opacity: logoHover ? 1 : 0, pointerEvents: logoHover ? 'auto' : 'none', color: 'var(--primary)' }}>
            <HomeOutlined style={{ fontSize: 24, marginRight: 8 }} />
            <span>主页</span>
          </span>
        </Link>

        <nav className="nav-menu" style={{ justifyContent: 'center' }}>{links}</nav>

        {/* 搜索框：桌面端输入框，移动端仅图标（跳转搜索页） */}
        <div className="nav-search">
          <Input
            allowClear
            size="middle"
            className="nav-search-input"
            prefix={<SearchOutlined style={{ color: 'var(--text-faint)' }} />}
            placeholder="搜索文章…"
            style={{ width: 180, borderRadius: 999 }}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onPressEnter={(e) => doSearch((e.target as HTMLInputElement).value)}
          />
          <span className="nav-icon-btn" title="搜索" onClick={() => navigate('/search')}>
            <SearchOutlined style={{ fontSize: 18 }} />
          </span>
        </div>

        {/* 音乐 */}
        <MusicPlayer panelOpen={musicOpen} onTogglePanel={() => setMusicOpen((o) => !o)} />

        {/* 亮度：亮色 / 暗色 / 护眼 / 跟随系统（图标随当前模式切换） */}
        <Popover trigger="click" placement="bottomRight" arrow={false} content={<BrightnessPanel />}>
          <span className="nav-icon-btn" title={`亮度：${themeOptions.find((o) => o.key === mode)?.label.replace(/^\S+\s/, '') ?? ''}`}>
            {modeIcons[mode]}
          </span>
        </Popover>

        {/* 外观：主题色 / 特效开关 */}
        <Popover trigger="click" placement="bottomRight" arrow={false} content={<AppearancePanel />}>
          <span className="nav-icon-btn" title="外观设置">
            <BgColorsOutlined style={{ fontSize: 18 }} />
          </span>
        </Popover>

        {/* 登录态 */}
        {user ? (
          <Dropdown
            trigger={['click']}
            menu={{
              items: [
                { key: 'admin', icon: <DashboardOutlined />, label: <Link to="/admin">管理后台</Link> },
                { key: 'logout', icon: <LogoutOutlined />, label: '退出登录' },
              ],
              onClick: ({ key }) => {
                if (key === 'logout') {
                  logout();
                  navigate('/');
                }
              },
            }}
          >
            <span style={{ display: 'inline-flex', alignItems: 'center', cursor: 'pointer', gap: 6 }}>
              <Avatar size={32} src={user.avatar} icon={<UserOutlined />} style={{ background: 'var(--primary-weak)' }} />
            </span>
          </Dropdown>
        ) : (
          <Link to="/login" className="nav-login-btn" title="登录 / 注册">
            <UserOutlined style={{ fontSize: 14 }} />
            <span className="nav-login-text">登录</span>
          </Link>
        )}

        {/* 移动端菜单 */}
        <span
          className="nav-link"
          style={{ display: 'none' }}
          id="mobile-menu-trigger"
          onClick={() => setMobileOpen(true)}
        >
          <MenuOutlined />
        </span>
        <style>{`@media (max-width: 900px){ #mobile-menu-trigger{ display:inline-flex !important; } .nav-menu{ display:none !important; } }`}</style>
      </div>

      <Drawer
        title={<span style={{ color: 'var(--primary)' }}>{siteConfig.name}</span>}
        placement="right"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        width={280}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <Link to="/" className="nav-link" onClick={() => setMobileOpen(false)}>主页</Link>
          <Link to="/articles" className="nav-link" onClick={() => setMobileOpen(false)}>全部文章</Link>
          <Link to="/articles?tab=category" className="nav-link" onClick={() => setMobileOpen(false)}>分类</Link>
          <Link to="/articles?tab=tag" className="nav-link" onClick={() => setMobileOpen(false)}>标签</Link>
          <Link to="/links" className="nav-link" onClick={() => setMobileOpen(false)}>友链</Link>
          <Link to="/mine" className="nav-link" onClick={() => setMobileOpen(false)}>我的</Link>
          <Link to="/about" className="nav-link" onClick={() => setMobileOpen(false)}>关于</Link>
          <Link to={user ? '/admin' : '/login'} className="nav-link" onClick={() => setMobileOpen(false)}>
            {user ? '管理后台' : '登录 / 注册'}
          </Link>
        </div>
      </Drawer>
    </header>
  );
}
