import { ReactNode } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Avatar } from 'antd';
import { EyeOutlined, LikeOutlined, MessageOutlined, ShareAltOutlined, ClockCircleOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { ArticleCard as ArticleCardType } from '../api/article';
import { UserCard } from '../api/auth';
import { CatLogo } from './Navbar';

export function DefaultAvatar({ user, size = 36 }: { user?: Partial<UserCard> | null; size?: number }) {
  return (
    <Avatar size={size} src={user?.avatar || '/chibi/furina.png'} style={{ background: 'var(--primary-weak)', flexShrink: 0 }}>
      {user?.nickname?.[0] || '狐'}
    </Avatar>
  );
}

export function fmtDuration(seconds?: number | null) {
  const s = seconds || 0;
  if (s < 60) return `${s}秒`;
  if (s < 3600) return `${Math.round(s / 60)}分钟`;
  return `${(s / 3600).toFixed(1)}小时`;
}

export default function ArticleCard({ article }: { article: ArticleCardType }) {
  const navigate = useNavigate();
  return (
    <div className="card card-hover article-card stagger" onClick={() => navigate(`/article/${article.id}`)}>
      {article.cover && <img className="cover" src={article.cover} alt={article.title} loading="lazy" />}
      <div className="body">
        <h3>{article.pinned && <span style={{ color: 'var(--primary)', marginRight: 6 }}>[置顶]</span>}{article.title}</h3>
        <p className="summary">{article.summary}</p>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {article.tags?.slice(0, 3).map((t) => (
            <span key={t.id} className="tag-chip" style={{ color: t.color || undefined, background: `${t.color || '#80a942'}1a` }}>
              {t.name}
            </span>
          ))}
        </div>
        <div className="meta">
          <span><LikeOutlined /> {article.likes}</span>
          <span><EyeOutlined /> {article.views}</span>
          <span><MessageOutlined /> {article.comments}</span>
          <span><ShareAltOutlined /> {article.shares}</span>
          <span><ClockCircleOutlined /> {fmtDuration(article.watchSeconds)}</span>
          {article.publishedAt && <span style={{ marginLeft: 'auto' }}>{dayjs(article.publishedAt).format('MM-DD')}</span>}
        </div>
      </div>
    </div>
  );
}

/** 侧边栏小部件容器 */
export function Widget({ title, icon, children }: { title: string; icon?: ReactNode; children: ReactNode }) {
  return (
    <div className="card sidebar-widget fade-in">
      <h4>{icon}{title}</h4>
      {children}
    </div>
  );
}

/** 页面底部 */
export function Footer() {
  return (
    <footer className="site-footer">
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 8 }}>
        <Link to="/"><CatLogo size={40} /></Link>
      </div>
      <div>
        © {new Date().getFullYear()} 迷失客客の博客 · 用 ❤ 和 🦊 构建
      </div>
      <div>
        前端样式参考开源主题 <a href="https://github.com/CuteLeaf/Firefly" target="_blank" rel="noreferrer">Firefly (MIT)</a>
      </div>
    </footer>
  );
}
