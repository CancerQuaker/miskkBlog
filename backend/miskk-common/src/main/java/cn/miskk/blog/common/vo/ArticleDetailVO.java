package cn.miskk.blog.common.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情 VO（包含正文与上下篇导航）。
 */
@Data
public class ArticleDetailVO {

    private Long id;
    private String title;
    private String summary;
    private String cover;
    private String content;
    private Long categoryId;
    private String categoryName;
    private java.util.List<TagVO> tags;
    private cn.miskk.blog.common.dto.UserCardVO author;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
    private Long watchSeconds;
    private Boolean pinned;
    private Boolean liked;
    /** 字数 */
    private Long wordCount;
    /** 预计阅读分钟数 */
    private Long readingMinutes;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long prevId;
    private String prevTitle;
    private Long nextId;
    private String nextTitle;
}
