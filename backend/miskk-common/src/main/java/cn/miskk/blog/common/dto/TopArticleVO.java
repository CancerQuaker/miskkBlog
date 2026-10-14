package cn.miskk.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门文章（管理仪表盘）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopArticleVO {

    private Long id;
    private String title;
    private Long views;
    private Long likes;
    private Long comments;
}
