package cn.miskk.blog.common.vo;

import cn.miskk.blog.common.dto.UserCardVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 VO（树形）。
 */
@Data
public class CommentVO {

    private Long id;
    private Long articleId;
    private Long parentId;
    private String content;
    private UserCardVO user;
    private UserCardVO replyUser;
    private LocalDateTime createdAt;
    private List<CommentVO> children;
}
