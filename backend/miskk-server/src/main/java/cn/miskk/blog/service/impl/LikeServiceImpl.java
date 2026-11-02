package cn.miskk.blog.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.miskk.blog.common.exception.BizException;
import cn.miskk.blog.document.ArticleDoc;
import cn.miskk.blog.document.ArticleLikeDoc;
import cn.miskk.blog.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 点赞服务实现（MongoDB）：(articleId, userId) 唯一索引兜底 + $inc 计数维护。
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean like(Long articleId, Long userId) {
        ArticleDoc article = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(articleId)
                .and("deleted").ne(true)), ArticleDoc.class);
        if (article == null || article.getStatus() != 1) {
            throw new BizException("文章不存在或未发布");
        }

        boolean exists = mongoTemplate.exists(Query.query(Criteria.where("articleId").is(articleId)
                .and("userId").is(userId)), ArticleLikeDoc.class);
        if (exists) {
            return false;
        }

        ArticleLikeDoc like = new ArticleLikeDoc();
        like.setId(IdUtil.getSnowflakeNextId());
        like.setArticleId(articleId);
        like.setUserId(userId);
        like.setCreatedAt(LocalDateTime.now());
        try {
            mongoTemplate.insert(like);
        } catch (DuplicateKeyException e) {
            return false;
        }

        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(articleId)),
                new Update().inc("likes", 1), ArticleDoc.class);
        return true;
    }

    @Override
    public boolean unlike(Long articleId, Long userId) {
        ArticleLikeDoc removed = mongoTemplate.findAndRemove(Query.query(Criteria.where("articleId").is(articleId)
                .and("userId").is(userId)), ArticleLikeDoc.class);
        if (removed != null) {
            ArticleDoc article = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(articleId)
                    .and("deleted").ne(true)), ArticleDoc.class);
            if (article != null && (article.getLikes() == null || article.getLikes() > 0)) {
                mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(articleId)),
                        new Update().inc("likes", -1), ArticleDoc.class);
            }
            return true;
        }
        return false;
    }
}
