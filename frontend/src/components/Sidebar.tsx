import { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { Avatar, Progress } from 'antd';
import {
  FireOutlined,
  FolderOutlined,
  TagsOutlined,
  UserOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { CategoryVO, TagVO } from '../api/article';
import { UserVO } from '../api/auth';
import { DefaultAvatar } from './Common';
import { ChibiImage } from './Chibi';
import { useAuthStore } from '../stores/authStore';
import { siteConfig } from '../config/site';

/** 左侧资料卡 + 分类标签 */
export default function Sidebar({
  profile,
  categories,
  tags,
}: {
  profile?: UserVO | null;
  categories?: CategoryVO[];
  tags?: TagVO[];
}) {
  const { user } = useAuthStore();
  const shown = profile || user;

  return (
    <aside className="layout-sidebar">
      <div className="card sidebar-widget fade-in" style={{ textAlign: 'center' }}>
        {/* 匿名时展示站长（管理员）资料卡，用芙宁娜头像；登录用户显示自己的头像，无头像回退默认图标 */}
        <Avatar size={84} src={shown ? shown.avatar : '/chibi/furina.png'} icon={<UserOutlined />} style={{ background: 'var(--primary-weak)', fontSize: 30 }}>
          {shown?.nickname?.[0]}
        </Avatar>
        <h3 style={{ margin: '12px 0 4px' }}>{shown?.nickname || '迷失客客'}</h3>
        <p style={{ color: 'var(--text-sub)', fontSize: 13, margin: 0 }}>
          {shown?.bio || '一只喜欢写代码的小狐狸捏~'}
        </p>
        <Link
          to="/links"
          className="tag-chip"
          style={{ marginTop: 12, textDecoration: 'none' }}
        >
          欢迎交换友链捏~
        </Link>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8, marginTop: 14 }}>
          <ChibiImage name="march7" size={64} style={{ animation: 'heroFloat 3.2s ease-in-out infinite' }} />
          <span style={{ fontSize: 12, color: 'var(--text-faint)', textAlign: 'left', lineHeight: 1.6 }}>
            同行的小伙伴<br />三月七 · 报道！
          </span>
        </div>
      </div>

      {categories && (
        <WidgetCard title="分类" icon={<FolderOutlined />}>
          {categories.map((c) => (
            <Link key={c.id} to={`/articles?categoryId=${c.id}`} className="cat-row">
              <span>{c.name}</span>
              <span className="count">{c.articleCount}</span>
            </Link>
          ))}
        </WidgetCard>
      )}

      {tags && (
        <WidgetCard title="标签" icon={<TagsOutlined />}>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {tags.slice(0, 12).map((t) => (
              <Link key={t.id} to={`/articles?tagId=${t.id}`} className="tag-chip" style={{ color: t.color || undefined }}>
                # {t.name}
              </Link>
            ))}
          </div>
        </WidgetCard>
      )}

      <WidgetCard title="站点信息" icon={<FireOutlined />}>
        <div style={{ color: 'var(--text-sub)', fontSize: 13, lineHeight: 2 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span><FileTextOutlined /> 运行时间</span>
            <span>{new Date().getFullYear() - siteConfig.startYear + 1} 天捏</span>
          </div>
          <div style={{ marginTop: 6 }}>
            <span style={{ color: 'var(--text-faint)', fontSize: 12 }}>
              {siteConfig.subtitle}
            </span>
          </div>
        </div>
      </WidgetCard>
    </aside>
  );
}

function WidgetCard({ title, icon, children }: { title: string; icon: ReactNode; children: ReactNode }) {
  return (
    <div className="card sidebar-widget fade-in">
      <h4>{icon}{title}</h4>
      {children}
    </div>
  );
}

export { WidgetCard };

/** 用于仪表盘等处的简单进度条 */
export function UsageProgress({ percent, label }: { percent: number; label: string }) {
  const color = percent > 85 ? '#ff4d4f' : percent > 65 ? '#faad14' : 'var(--primary)';
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
        <span>{label}</span>
        <span style={{ color }}>{percent.toFixed(1)}%</span>
      </div>
      <Progress percent={percent} showInfo={false} strokeColor={color} size={['100%', 8]} />
    </div>
  );
}

export { DefaultAvatar };
