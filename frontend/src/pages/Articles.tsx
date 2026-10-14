import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Spin, Empty, Pagination, Tabs, Badge } from 'antd';
import { articleApi, ArticleCard as CardType, CategoryVO, TagVO } from '../api/article';
import ArticleCard from '../components/Common';
import Sidebar from '../components/Sidebar';
import { useNavigate } from 'react-router-dom';

/**
 * 文章页：全部文章（时间倒序）/ 分类 / 标签 三大入口。
 */
export default function Articles() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const tab = params.get('tab') || 'all';
  const categoryId = params.get('categoryId');
  const tagId = params.get('tagId');

  const [articles, setArticles] = useState<CardType[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [tags, setTags] = useState<TagVO[]>([]);

  useEffect(() => {
    articleApi.categories().then(setCategories).catch(() => {});
    articleApi.tags().then(setTags).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    const query: Record<string, any> = { page, size: 9 };
    if (categoryId) query.categoryId = categoryId;
    if (tagId) query.tagId = tagId;
    articleApi.page(query)
      .then((d) => {
        setArticles(d.records || []);
        setTotal(d.total);
      })
      .finally(() => setLoading(false));
  }, [page, categoryId, tagId]);

  const activeCat = categories.find((c) => String(c.id) === categoryId);
  const activeTag = tags.find((t) => String(t.id) === tagId);

  return (
    <div className="page-container">
      <div className="layout-with-sidebar">
        <Sidebar categories={categories} tags={tags} />
        <main>
          <Tabs
            activeKey={tab}
            onChange={(k) => {
              setPage(1);
              navigate(k === 'all' ? '/articles' : `/articles?tab=${k}`);
            }}
            items={[
              { key: 'all', label: '全部文章' },
              { key: 'category', label: '分类' },
              { key: 'tag', label: '标签' },
            ]}
          />

          {tab === 'category' && (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 16 }}>
              {categories.map((c) => (
                <a
                  key={c.id}
                  className="tag-chip"
                  style={{
                    fontSize: 13.5, padding: '5px 14px', textDecoration: 'none',
                    background: String(c.id) === categoryId ? 'var(--primary)' : 'var(--primary-weak)',
                    color: String(c.id) === categoryId ? '#fff' : 'var(--primary)',
                  }}
                  onClick={() => { setPage(1); navigate(`/articles?tab=category&categoryId=${c.id}`); }}
                >
                  <Badge count={c.articleCount} size="small" color="rgba(255,255,255,.4)" offset={[4, -2]}>
                    {c.name}
                  </Badge>
                </a>
              ))}
            </div>
          )}

          {tab === 'tag' && (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 16 }}>
              {tags.map((t) => (
                <a
                  key={t.id}
                  className="tag-chip"
                  style={{
                    fontSize: 13.5, padding: '5px 14px', textDecoration: 'none',
                    background: String(t.id) === tagId ? (t.color || 'var(--primary)') : 'var(--primary-weak)',
                    color: String(t.id) === tagId ? '#fff' : (t.color || 'var(--primary)'),
                  }}
                  onClick={() => { setPage(1); navigate(`/articles?tab=tag&tagId=${t.id}`); }}
                >
                  # {t.name} · {t.articleCount}
                </a>
              ))}
            </div>
          )}

          {(activeCat || activeTag) && (
            <p style={{ color: 'var(--text-sub)', marginTop: 0 }}>
              当前筛选：{activeCat ? `分类「${activeCat.name}」` : `标签「${activeTag!.name}」`} · {total} 篇
              <a style={{ color: 'var(--primary)', marginLeft: 10 }} onClick={() => navigate('/articles')}>清除</a>
            </p>
          )}

          {loading ? (
            <div style={{ textAlign: 'center', padding: 60 }}><Spin /></div>
          ) : articles.length === 0 ? (
            <Empty description="暂无文章捏~" />
          ) : (
            <div className="article-grid stagger">
              {articles.map((a) => <ArticleCard key={a.id} article={a} />)}
            </div>
          )}
          <div style={{ textAlign: 'center', marginTop: 24 }}>
            <Pagination current={page} total={total} pageSize={9} onChange={setPage} hideOnSinglePage />
          </div>
        </main>
      </div>
    </div>
  );
}
