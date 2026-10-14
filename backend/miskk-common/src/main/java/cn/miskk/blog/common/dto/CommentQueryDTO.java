package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论分页查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentQueryDTO extends PageQueryDTO {

    @NotNull(message = "articleId 不能为空")
    private Long articleId;
}
