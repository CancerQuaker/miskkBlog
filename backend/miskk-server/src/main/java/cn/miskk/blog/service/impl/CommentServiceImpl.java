package cn.miskk.blog.service.impl;

import cn.miskk.blog.entity.Article;
import cn.miskk.blog.entity.Comment;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.mapper.CommentMapper;
import cn.miskk.blog.service.CommentService;
import cn.miskk.blog.service.UserCardService;
import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评论服务实现：两级楼层数据（顶级评论分页 + 楼中楼平铺）。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserCardService userCardService;

    @Override
    public PageVO<CommentVO> pageByArticle(Long articleId, long page, long size) {
        Page<Comment> p = new Page<>(page, Math.min(size, 20));
        Page<Comment> topPage = commentMapper.selectPage(p, Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreatedAt));

        List<Comment> tops = topPage.getRecords();
        List<CommentVO> topVos = new ArrayList<>();
        if (!tops.isEmpty()) {
            List<Long> topIds = tops.stream().map(Comment::getId).toList();
            List<Comment> children = commentMapper.selectList(Wrappers.<Comment>lambdaQuery()
                    .eq(Comment::getArticleId, articleId)
                    .in(Comment::getParentId, topIds)
                    .orderByAsc(Comment::getCreatedAt));

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
            for (Comment child : children) {
                CommentVO vo = toVO(child, cards);
                childrenMap.computeIfAbsent(child.getParentId(), k -> new ArrayList<>()).add(vo);
            }
            for (Comment top : tops) {
                CommentVO vo = toVO(top, cards);
                vo.setChildren(childrenMap.getOrDefault(top.getId(), List.of()));
                topVos.add(vo);
            }
        }

        PageVO<CommentVO> vo = new PageVO<>();
        vo.setRecords(topVos);
        vo.setTotal(topPage.getTotal());
        vo.setPage(topPage.getCurrent());
        vo.setSize(topPage.getSize());
        return vo;
    }

    @Override
    @Transactional
    public CommentVO create(Long articleId, Long userId, String content, Long parentId, Long replyUserId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStatus() != 1) {
            throw new BizException("文章不存在或未发布");
        }

        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
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
        comment.setDeleted(0);
        commentMapper.insert(comment);

        Article update = new Article();
        update.setId(articleId);
        update.setComments((article.getComments() == null ? 0 : article.getComments()) + 1);
        articleMapper.updateById(update);

        List<Long> cardIds = new ArrayList<>();
        cardIds.add(userId);
        if (replyUserId != null) {
            cardIds.add(replyUserId);
        }
        Map<Long, UserCardVO> cards = userCardService.getCards(cardIds);
        return toVO(comment, cards);
    }

    @Override
    @Transactional
    public void delete(Long commentId, LoginUser user) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BizException("评论不存在");
        }
        boolean owner = comment.getUserId().equals(user.getUserId());
        if (!owner && !user.isAdmin()) {
            throw new BizException("只能删除自己的评论");
        }

        long removed = 1;
        if (comment.getParentId() == null) {
            List<Comment> children = commentMapper.selectList(Wrappers.<Comment>lambdaQuery()
                    .eq(Comment::getParentId, commentId));
            for (Comment child : children) {
                commentMapper.deleteById(child.getId());
            }
            removed += children.size();
        }
        commentMapper.deleteById(commentId);

        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            Article update = new Article();
            update.setId(article.getId());
            update.setComments(Math.max(0, (article.getComments() == null ? 0 : article.getComments()) - removed));
            articleMapper.updateById(update);
        }
    }

    private CommentVO toVO(Comment comment, Map<Long, UserCardVO> cards) {
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
