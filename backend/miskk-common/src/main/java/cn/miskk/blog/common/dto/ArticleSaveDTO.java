package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章保存（新增/编辑）请求。
 */
@Data
public class ArticleSaveDTO {

    /** 文章 ID：编辑时必传，新增时为空 */
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多 100 个字符")
    private String title;

    @Size(max = 200, message = "摘要最多 200 个字符")
    private String summary;

    private String cover;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @NotNull(message = "请选择分类")
    private Long categoryId;

    /** 标签名称列表，不存在时自动创建 */
    private List<String> tagNames;

    /** 0 草稿 1 发布 */
    @NotNull(message = "请选择文章状态")
    private Integer status;

    private Boolean pinned;
}
