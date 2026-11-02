package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章文档（MongoDB collection: article）。
 * <p>
 * ID 保持 Long（雪花算法生成，种子数据用小固定值），对外 REST 路径 /api/article/articles/{id} 不变；
 * 标签以 id 数组内嵌（替代原 t_article_tag 关联表）；deleted 为 true 表示逻辑删除。
 */
@Data
@Document("article")
public class ArticleDoc {

    @Id
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
    @Indexed
    private Integer status;

    /** 是否置顶（1 置顶） */
    private Integer pinned;

    private Long views;

    private Long likes;

    private Long comments;

    private Long shares;

    /** 累计在线观看时长（秒） */
    private Long watchSeconds;

    /** 标签 ID 内嵌数组（对应 tag 集合） */
    @Indexed
    private List<Long> tags;

    /** 逻辑删除标记（true 已删除，查询一律过滤） */
    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;
}
