package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求。
 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "文章不能为空")
    private Long articleId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论最多 1000 个字符")
    private String content;

    /** 回复的父评论 ID（顶级评论为空） */
    private Long parentId;

    /** 楼中楼回复的目标用户 */
    private Long replyUserId;
}
