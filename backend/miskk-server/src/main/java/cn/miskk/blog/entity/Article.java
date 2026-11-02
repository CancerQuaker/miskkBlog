package cn.miskk.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体（t_article）。
 */
@Data
@TableName("t_article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    /** Markdown 正文 */
    private String content;

    /** 封面图 URL */
    private String cover;

    private Long categoryId;

    private Long authorId;

    /** 0 草稿 1 已发布 2 已隐藏 */
    private Integer status;

    /** 是否置顶 */
    private Integer pinned;

    private Long views;

    private Long likes;

    private Long comments;

    private Long shares;

    /** 累计在线观看时长（秒） */
    private Long watchSeconds;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;
}
