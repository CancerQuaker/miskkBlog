package cn.miskk.blog.common.vo;

import lombok.Data;

/**
 * 标签 VO。
 */
@Data
public class TagVO {

    private Long id;
    private String name;
    private String color;
    private Long articleCount;
}
