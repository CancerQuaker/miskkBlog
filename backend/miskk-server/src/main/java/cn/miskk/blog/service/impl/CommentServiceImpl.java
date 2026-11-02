package cn.miskk.blog.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.exception.BizException;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.document.ArticleDoc;
import cn.miskk.blog.document.CommentDoc;
import cn.miskk.blog.service.CommentService;
import cn.miskk.blog.service.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论服务实现（MongoDB）：两级楼层数据（顶级评论分页 + 楼中楼平铺）。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final MongoTemplate mongoTemplate;
    private final UserCardService userCardService;

    @Override
    public PageVO<CommentVO> pageByArticle(Long articleId, long page, long size) {
        int limit = (int) Math.min(size, 20);
        Criteria topCriteria = Criteria.where("deleted").ne(true)
                .and("articleId").is(articleId)
                .and("parentId").is(null);
        Query topQuery = Query.query(topCriteria)
                .skip((page - 1) * limit).limit(limit)
                .with(Sort.by(Sort.Order.desc("createdAt")));
        long total = mongoTemplate.count(Query.query(topCriteria), CommentDoc.class);
        List<CommentDoc> tops = mongoTemplate.find(topQuery, CommentDoc.class);

        List<CommentVO> topVos = new ArrayList<>();
        if (!tops.isEmpty()) {
            List<Long> topIds = tops.stream().map(CommentDoc::getId).toList();
            List<CommentDoc> children = mongoTemplate.find(Query.query(Criteria.where("deleted").ne(true)
                            .and("articleId").is(articleId)
                            .and("parentId").in(topIds))
                    .with(Sort.by(Sort.Order.asc("createdAt"))), CommentDoc.class);

            // 组装用户名片
            List<Long> userIds = new ArrayList<>();
            tops.forEach(c -> userIds.add(c.getUserId()));
            children.forEach(c -> {
                userIds.add(c.getUserId());
                if (c.getReplyUserId() != null) {
                    userIds.add(c.getReplyUserId());
                }
            });
            Map<Long, UserCardVO> cards = userCardService.getCards(userIds);

            Map<Long, List<CommentVO>> childrenMap = new HashMap<>();
            for (CommentDoc child : children) {
                CommentVO vo = toVO(child, cards);
                childrenMap.computeIfAbsent(child.getParentId(), k -> new ArrayList<>()).add(vo);
            }
            for (CommentDoc top : tops) {
                CommentVO vo = toVO(top, cards);
                vo.setChildren(childrenMap.getOrDefault(top.getId(), List.of()));
                topVos.add(vo);
            }
        }

        PageVO<CommentVO> vo = new PageVO<>();
        vo.setRecords(topVos);
        vo.setTotal(total);
        vo.setPage(page);
        vo.setSize(limit);
        return vo;
    }

    @Override
    public CommentVO create(Long articleId, Long userId, String content, Long parentId, Long replyUserId) {
        ArticleDoc article = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(articleId)
                .and("deleted").ne(true)), ArticleDoc.class);
        if (article == null || article.getStatus() != 1) {
            throw new BizException("文章不存在或未发布");
        }

        LocalDateTime now = LocalDateTime.now();
        CommentDoc comment = new CommentDoc();
        comment.setId(IdUtil.getSnowflakeNextId());
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        if (parentId != null) {
            CommentDoc parent = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(parentId)
                    .and("deleted").ne(true)), CommentDoc.class);
            if (parent == null || !parent.getArticleId().equals(articleId)) {
                throw new BizException("要回复的评论不存在");
            }
            // 二级以上回复统一挂到顶级评论，保留被回复人
            comment.setParentId(parent.getParentId() == null ? parent.getId() : parent.getParentId());
        }
        if (replyUserId != null && replyUserId.equals(userId)) {
            comment.setReplyUserId(null);
        } else {
            comment.setReplyUserId(replyUserId);
        }
        comment.setDeleted(false);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        mongoTemplate.insert(comment);

        incArticleCounter(articleId, "comments", 1);

        List<Long> cardIds = new ArrayList<>();
        cardIds.add(userId);
        if (replyUserId != null) {
            cardIds.add(replyUserId);
        }
        Map<Long, UserCardVO> cards = userCardService.getCards(cardIds);
        return toVO(comment, cards);
    }

    @Override
    public void delete(Long commentId, LoginUser user) {
        CommentDoc comment = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(commentId)
                .and("deleted").ne(true)), CommentDoc.class);
        if (comment == null) {
            throw new BizException("评论不存在");
        }
        boolean owner = comment.getUserId().equals(user.getUserId());
        if (!owner && !user.isAdmin()) {
            throw new BizException("只能删除自己的评论");
        }

        LocalDateTime now = LocalDateTime.now();
        Update logicDelete = new Update().set("deleted", true).set("updatedAt", now);
        long removed = 1;
        if (comment.getParentId() == null) {
            List<CommentDoc> children = mongoTemplate.find(Query.query(Criteria.where("parentId").is(commentId)
                    .and("deleted").ne(true)), CommentDoc.class);
            for (CommentDoc child : children) {
                mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(child.getId())), logicDelete, CommentDoc.class);
            }
            removed += children.size();
        }
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(commentId)), logicDelete, CommentDoc.class);

        if (articleExists(comment.getArticleId())) {
            incArticleCounter(comment.getArticleId(), "comments", -removed);
        }
    }

    private boolean articleExists(Long articleId) {
        return mongoTemplate.exists(Query.query(Criteria.where("_id").is(articleId)
                .and("deleted").ne(true)), ArticleDoc.class);
    }

    /** 文章计数原子增减（负数减少） */
    private void incArticleCounter(Long articleId, String field, long delta) {
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(articleId).and("deleted").ne(true)),
                new Update().inc(field, delta), ArticleDoc.class);
    }

    private CommentVO toVO(CommentDoc comment, Map<Long, UserCardVO> cards) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUser(cards.getOrDefault(comment.getUserId(),
                new UserCardVO(comment.getUserId(), "喵友" + comment.getUserId(), null)));
        if (comment.getReplyUserId() != null) {
            vo.setReplyUser(cards.get(comment.getReplyUserId()));
        }
        return vo;
    }
}
