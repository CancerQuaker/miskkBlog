package cn.miskk.blog.service.impl;

import cn.miskk.blog.entity.Article;
import cn.miskk.blog.entity.ArticleLike;
import cn.miskk.blog.mapper.ArticleLikeMapper;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.service.LikeService;
import cn.miskk.blog.common.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现：唯一索引兜底 + 计数维护。
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final ArticleLikeMapper likeMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public boolean like(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStatus() != 1) {
            throw new BizException("文章不存在或未发布");
        }

        Long exists = likeMapper.selectCount(Wrappers.<ArticleLike>lambdaQuery()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId));
        if (exists != null && exists > 0) {
            return false;
        }

        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(userId);
        try {
            likeMapper.insert(like);
        } catch (DuplicateKeyException e) {
            return false;
        }

        Article update = new Article();
        update.setId(articleId);
        update.setLikes((article.getLikes() == null ? 0 : article.getLikes()) + 1);
        articleMapper.updateById(update);
        return true;
    }

    @Override
    @Transactional
    public boolean unlike(Long articleId, Long userId) {
        int deleted = likeMapper.delete(Wrappers.<ArticleLike>lambdaQuery()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId));
        if (deleted > 0) {
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                Article update = new Article();
                update.setId(articleId);
                update.setLikes(Math.max(0, (article.getLikes() == null ? 0 : article.getLikes()) - 1));
                articleMapper.updateById(update);
            }
            return true;
        }
        return false;
    }
}
