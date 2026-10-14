package cn.miskk.blog.service;

import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.dto.ArticleStatsVO;

/**
 * 文章服务。
 */
public interface ArticleService {

    PageVO<ArticleCardVO> page(long page, long size, Long categoryId, Long tagId,
                               String keyword, boolean publishedOnly);

    ArticleDetailVO detail(Long id, LoginUser login);

    PageVO<ArticleCardVO> search(String keyword, long page, long size);

    ArticleDetailVO save(ArticleSaveDTO dto, Long authorId, Long articleId);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    /** 记录一次浏览（按访客去重），返回是否为新访客 */
    boolean recordView(Long articleId, String visitorIdentity);

    /** 将 Redis 缓冲的浏览量增量落库 */
    void flushViews();

    ArticleStatsVO statsOverview();
}
