package cn.miskk.blog.common.vo;

import cn.miskk.blog.common.dto.UserCardVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章卡片（列表页使用，不包含正文）。
 */
@Data
public class ArticleCardVO {

    private Long id;
    private String title;
    private String summary;
    private String cover;
    private Long categoryId;
    private String categoryName;
    private List<TagVO> tags;
    private UserCardVO author;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
    private Long watchSeconds;
    private Boolean pinned;
    /** 0 草稿 1 已发布 2 已隐藏（管理端使用） */
    private Integer status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
