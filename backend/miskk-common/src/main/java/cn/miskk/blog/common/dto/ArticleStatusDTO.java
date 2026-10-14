package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改文章状态请求：0 草稿 / 1 已发布 / 2 已隐藏。
 */
@Data
public class ArticleStatusDTO {

    /** 文章 ID */
    @NotNull(message = "文章 ID 不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
