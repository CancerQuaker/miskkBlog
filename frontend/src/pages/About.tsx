import { Avatar, Tag } from 'antd';
import { GithubOutlined, MailOutlined, RocketOutlined, SmileOutlined } from '@ant-design/icons';
import MarkdownView from '../components/MarkdownView';
import { ChibiImage, CHIBI_LIST } from '../components/Chibi';
import { aboutConfig, siteConfig } from '../config/site';

/**
 * 关于页：作者本人 + 项目介绍。
 */
export default function About() {
  const { author, project } = aboutConfig;
  const projectMd = [
    '# 项目介绍',
    '',
    ...project.intro.map((p) => `${p}\n`),
    '| 模块 | 技术 |',
    '|------|------|',
    ...project.techs.map((t) => `| ${t.label} | ${t.value} |`),
  ].join('\n');

  return (
    <div className="page-container" style={{ maxWidth: 860 }}>
      {/* 作者卡片 */}
      <div className="card fade-in" style={{ padding: '30px', textAlign: 'center', marginBottom: 20 }}>
        <Avatar size={96} src="/chibi/furina.png" style={{ background: 'var(--primary-weak)', fontSize: 38, fontWeight: 700 }}>
          {author.avatarText}
        </Avatar>
        <h1 style={{ margin: '14px 0 6px' }}>{author.name}</h1>
        <p style={{ color: 'var(--text-sub)', margin: 0 }}>{author.bio}</p>
        <div style={{ marginTop: 14, display: 'flex', justifyContent: 'center', flexWrap: 'wrap', gap: 8 }}>
          {author.skills.map((s) => (
            <Tag key={s} color="pink" style={{ borderRadius: 999 }}>{s}</Tag>
          ))}
        </div>
        <div style={{ marginTop: 18, display: 'flex', justifyContent: 'center', gap: 12 }}>
          <a href={author.socials.github} target="_blank" rel="noreferrer" className="nav-link">
            <GithubOutlined /> GitHub
          </a>
          <a href={`mailto:${author.socials.email}`} className="nav-link">
            <MailOutlined /> {author.socials.email}
          </a>
        </div>
      </div>

      {/* 站内出没的 Q 版小伙伴 */}
      <div className="card fade-in" style={{ padding: '26px 30px', marginBottom: 20 }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <SmileOutlined style={{ color: 'var(--primary)' }} /> 站内出没的小伙伴们
        </h2>
        <p style={{ color: 'var(--text-sub)', fontSize: 13, marginTop: 0 }}>
          来自《原神》与《崩坏：星穹铁道》的 Q 版小伙伴会在站内各处闪现（素材为官方表情/贴纸，仅作装饰）
        </p>
        <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '14px 18px' }}>
          {Object.keys(CHIBI_LIST).map((key, i) => (
            <span key={key} style={{ animation: `heroFloat ${3 + (i % 5) * 0.35}s ease-in-out ${(i % 7) * 0.25}s infinite`, display: 'inline-block' }}>
              <ChibiImage name={key as keyof typeof CHIBI_LIST} size={72} showName />
            </span>
          ))}
        </div>
      </div>

      {/* 项目介绍 */}
      <div className="card fade-in" style={{ padding: '26px 30px' }}>
        <h2 style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <RocketOutlined style={{ color: 'var(--primary)' }} /> 关于本项目
        </h2>
        <MarkdownView content={projectMd} />
        <p style={{ color: 'var(--text-faint)', fontSize: 13 }}>
          本站为个人学习项目 · {siteConfig.name} · 前端样式参考开源主题 Firefly (MIT)
        </p>
      </div>
    </div>
  );
}
