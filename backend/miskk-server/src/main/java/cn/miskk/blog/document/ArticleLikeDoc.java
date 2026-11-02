package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 文章点赞记录文档（MongoDB collection: article_like）；
 * (articleId, userId) 唯一索引兜底并发重复点赞。
 */
@Data
@Document("article_like")
@CompoundIndex(name = "uk_article_user", def = "{'articleId': 1, 'userId': 1}", unique = true)
public class ArticleLikeDoc {

    @Id
    private Long id;

    private Long articleId;

    private Long userId;

    private LocalDateTime createdAt;
}
