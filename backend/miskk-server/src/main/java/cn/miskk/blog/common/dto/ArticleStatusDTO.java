package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改文章状态请求：0 草稿 / 1 已发布 / 2 已隐藏。
 */
@Data
public class ArticleStatusDTO {

    /** 文章 ID（以路径变量为准，请求体中可省略） */
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
