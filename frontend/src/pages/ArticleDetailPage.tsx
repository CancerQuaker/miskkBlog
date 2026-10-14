import { useEffect, useRef, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Spin, Result, Button, message } from 'antd';
import {
  EyeOutlined, MessageOutlined, ShareAltOutlined, FieldTimeOutlined,
  FileTextOutlined, ArrowLeftOutlined, ArrowRightOutlined, CalendarOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { articleApi, ArticleDetail } from '../api/article';
import MarkdownView, { TocItem, extractToc } from '../components/MarkdownView';
import CommentSection, { LikeButton } from '../components/CommentSection';
import ShareButtons from '../components/ShareButtons';
import { DefaultAvatar, fmtDuration } from '../components/Common';
import { useAuthStore } from '../stores/authStore';

export default function ArticleDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [article, setArticle] = useState<ArticleDetail | null>(null);
  const [error, setError] = useState('');
  const [liked, setLiked] = useState(false);
  const [toc, setToc] = useState<TocItem[]>([]);
  const watchSeconds = useRef(0);

  useEffect(() => {
    if (!id) return;
    setArticle(null);
    setError('');
    window.scrollTo(0, 0);
    articleApi.detail(id)
      .then((d) => {
        setArticle(d);
        setLiked(!!d.liked);
        setToc(extractToc(d.content));
        // 上报浏览（去重由后端处理）
        articleApi.view(id).catch(() => {});
      })
      .catch((e) => setError(e.message));
  }, [id, user?.id]);

  // 在线观看时长心跳：每 20 秒上报一次（页面可见时）
  useEffect(() => {
    if (!id) return;
    const timer = setInterval(() => {
      if (document.visibilityState === 'visible') {
        const seconds = Math.min(20, watchSeconds.current + 20);
        watchSeconds.current = 0;
        if (seconds > 0) articleApi.watch(id, seconds).catch(() => {});
      }
    }, 20000);
    const onLeave = () => {
      if (watchSeconds.current > 0) {
        navigator.sendBeacon?.(
          `/api/article/articles/${id}/watch`,
          new Blob([JSON.stringify({ seconds: watchSeconds.current })], { type: 'application/json' }),
        );
        watchSeconds.current = 0;
      }
    };
    window.addEventListener('pagehide', onLeave);
    return () => {
      clearInterval(timer);
      window.removeEventListener('pagehide', onLeave);
      onLeave();
    };
  }, [id]);

  if (error) {
    return <Result status="404" title="文章不存在" subTitle={error}
      extra={<Button type="primary" onClick={() => navigate('/articles')}>回到文章列表</Button>} />;
  }
  if (!article) {
    return <div style={{ textAlign: 'center', padding: 120 }}><Spin size="large" /></div>;
  }

  const likeChange = (isLiked: boolean, count: number) => {
    setLiked(isLiked);
    setArticle({ ...article, likes: count });
  };

  return (
    <div className="page-container">
      <div className="layout-with-sidebar article-layout">
        {/* 右侧目录（桌面端显示） */}
        <aside className="layout-sidebar toc-sidebar">
          {toc.length > 0 && (
            <div className="card sidebar-widget fade-in">
              <h4>📑 目录</h4>
              <div style={{ maxHeight: '50vh', overflowY: 'auto', fontSize: 13.5 }}>
                {toc.map((t, i) => (
                  <div
                    key={i}
                    onClick={() => document.getElementById(t.id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })}
                    style={{
                      marginLeft: (t.level - 1) * 14,
                      padding: '4px 0',
                      cursor: 'pointer',
                      color: t.level <= 2 ? 'var(--text-main)' : 'var(--text-sub)',
                      fontWeight: t.level === 1 ? 600 : 400,
                    }}
                  >
                    {t.text}
                  </div>
                ))}
              </div>
            </div>
          )}
        </aside>

        <main style={{ minWidth: 0 }}>
          <article className="card fade-in" style={{ padding: '28px 30px' }}>
            {article.cover && (
              <img src={article.cover} alt={article.title}
                style={{ width: '100%', borderRadius: 'var(--radius-md)', marginBottom: 20, maxHeight: 380, objectFit: 'cover' }} />
            )}

            <h1 style={{ margin: '0 0 10px', fontSize: 26, lineHeight: 1.4 }}>
              {article.pinned && <span style={{ color: 'var(--primary)', marginRight: 8 }}>[置顶]</span>}
              {article.title}
            </h1>

            <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap', color: 'var(--text-sub)', fontSize: 13.5 }}>
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                <DefaultAvatar user={article.author} size={26} />
                {article.author?.nickname || '佚名'}
              </span>
              {article.publishedAt && <span><CalendarOutlined /> {dayjs(article.publishedAt).format('YYYY-MM-DD HH:mm')}</span>}
              <span><EyeOutlined /> {article.views} 浏览</span>
              <span><MessageOutlined /> {article.comments} 评论</span>
              <span><ShareAltOutlined /> {article.shares} 分享</span>
              <span><FieldTimeOutlined /> 累计观看 {fmtDuration(article.watchSeconds)}</span>
              <span><FileTextOutlined /> {article.readingMinutes} 分钟阅读</span>
            </div>

            <div style={{ display: 'flex', gap: 8, margin: '14px 0 4px', flexWrap: 'wrap' }}>
              <Link to={`/articles?tab=category&categoryId=${article.categoryId}`} className="tag-chip" style={{ textDecoration: 'none' }}>
                📁 {article.categoryName}
              </Link>
              {article.tags?.map((t) => (
                <Link key={t.id} to={`/articles?tab=tag&tagId=${t.id}`} className="tag-chip" style={{ color: t.color || undefined, textDecoration: 'none' }}>
                  # {t.name}
                </Link>
              ))}
            </div>

            <hr style={{ border: 'none', borderTop: '1px solid var(--divider)', margin: '16px 0' }} />

            <MarkdownView content={article.content} />

            <hr style={{ border: 'none', borderTop: '1px solid var(--divider)', margin: '24px 0 18px' }} />

            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 18 }}>
              <LikeButton articleId={article.id} liked={liked} count={article.likes} onChange={likeChange} />
            </div>

            <div style={{ display: 'flex', justifyContent: 'center' }}>
              <ShareButtons articleId={article.id} title={article.title} />
            </div>
          </article>

          {/* 上一篇 / 下一篇 */}
          <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, marginTop: 16 }}>
            {article.prevId ? (
              <Link to={`/article/${article.prevId}`} className="card card-hover" style={{ padding: '12px 16px', flex: 1 }}>
                <span style={{ color: 'var(--text-faint)', fontSize: 12 }}><ArrowLeftOutlined /> 上一篇</span>
                <div style={{ fontWeight: 600, marginTop: 4 }}>{article.prevTitle}</div>
              </Link>
            ) : <div style={{ flex: 1 }} />}
            {article.nextId ? (
              <Link to={`/article/${article.nextId}`} className="card card-hover" style={{ padding: '12px 16px', flex: 1, textAlign: 'right' }}>
                <span style={{ color: 'var(--text-faint)', fontSize: 12 }}>下一篇 <ArrowRightOutlined /></span>
                <div style={{ fontWeight: 600, marginTop: 4 }}>{article.nextTitle}</div>
              </Link>
            ) : <div style={{ flex: 1 }} />}
          </div>

          <CommentSection articleId={article.id} />
        </main>
      </div>
    </div>
  );
}
