package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 评论文档（MongoDB collection: comment）；两级楼层（顶级 parentId 为空 + 楼中楼挂顶级）。
 */
@Data
@Document("comment")
@CompoundIndex(name = "idx_article_created", def = "{'articleId': 1, 'createdAt': -1}")
public class CommentDoc {

    @Id
    private Long id;

    private Long articleId;

    private Long userId;

    /** 父评论 ID（顶级为空） */
    @Indexed
    private Long parentId;

    /** 被回复用户 ID */
    private Long replyUserId;

    private String content;

    /** 逻辑删除标记 */
    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
