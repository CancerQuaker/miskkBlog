import { useEffect, useState } from 'react';
import { Spin, Empty, Pagination } from 'antd';
import { articleApi, ArticleCard as CardType, CategoryVO, TagVO } from '../api/article';
import ArticleCard from '../components/Common';
import Sidebar from '../components/Sidebar';
import { ChibiImage } from '../components/Chibi';
import { siteConfig } from '../config/site';

export default function Home() {
  const [articles, setArticles] = useState<CardType[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [tags, setTags] = useState<TagVO[]>([]);

  useEffect(() => {
    setLoading(true);
    articleApi.page({ page, size: 8 })
      .then((d) => {
        setArticles(d.records || []);
        setTotal(d.total);
      })
      .finally(() => setLoading(false));
    articleApi.categories().then(setCategories).catch(() => {});
    articleApi.tags().then(setTags).catch(() => {});
  }, [page]);

  return (
    <div>
      <div className="page-container">
        {/* Hero 横幅 */}
        <div className="hero fade-in">
          {/* 背景星光装饰 */}
          <span className="hero-star" style={{ left: '12%', top: '22%' }} />
          <span className="hero-star" style={{ left: '30%', top: '68%', animationDelay: '0.8s' }} />
          <span className="hero-star" style={{ left: '55%', top: '18%', animationDelay: '1.6s' }} />
          <span className="hero-star" style={{ left: '70%', top: '60%', animationDelay: '0.4s' }} />
          <span className="hero-star" style={{ left: '88%', top: '30%', animationDelay: '1.2s' }} />
          <h1>{siteConfig.name}</h1>
          <p>{siteConfig.subtitle}</p>
          <ChibiImage name="paimon" size={100} className="hero-paimon" />
        </div>

        <div className="layout-with-sidebar" style={{ marginTop: 20 }}>
          <Sidebar categories={categories} tags={tags} />

          <main>
            <h2 style={{ margin: '0 0 14px', fontSize: 20 }}>
              🌸 最新文章
              <span style={{ color: 'var(--text-faint)', fontSize: 13, marginLeft: 10 }}>共 {total} 篇</span>
            </h2>
            {loading ? (
              <div style={{ textAlign: 'center', padding: 60 }}><Spin /></div>
            ) : articles.length === 0 ? (
              <Empty description="还没有文章，快去后台写一篇捏~" />
            ) : (
              <div className="article-grid stagger">
                {articles.map((a) => <ArticleCard key={a.id} article={a} />)}
              </div>
            )}
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination current={page} total={total} pageSize={8} onChange={setPage} hideOnSinglePage />
            </div>
          </main>
        </div>
      </div>
    </div>
  );
}
