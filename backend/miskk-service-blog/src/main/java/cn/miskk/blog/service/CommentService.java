package cn.miskk.blog.service;

import cn.miskk.blog.common.vo.CommentVO;
import cn.miskk.blog.common.api.PageVO;

/**
 * 评论服务。
 */
public interface CommentService {

    PageVO<CommentVO> pageByArticle(Long articleId, long page, long size);

    CommentVO create(Long articleId, Long userId, String content, Long parentId, Long replyUserId);

    void delete(Long commentId, cn.miskk.blog.common.auth.LoginUser user);
}
