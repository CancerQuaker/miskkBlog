package cn.miskk.blog.common.vo;

import lombok.Data;

/**
 * 分类 VO。
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private String description;
    /** 已发布文章数 */
    private Long articleCount;
}
