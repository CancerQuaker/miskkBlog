package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 观看时长记录文档（MongoDB collection: watch_record）；按 文章+日期+访客 每日一条累加。
 */
@Data
@Document("watch_record")
@CompoundIndex(name = "idx_article_date_user", def = "{'articleId': 1, 'watchDate': 1, 'userId': 1}")
@CompoundIndex(name = "idx_article_date_visitor", def = "{'articleId': 1, 'watchDate': 1, 'visitorKey': 1}")
public class WatchRecordDoc {

    @Id
    private Long id;

    private Long articleId;

    /** 用户 ID（匿名访客为空） */
    private Long userId;

    /** 匿名访客指纹（IP 哈希，登录用户为空） */
    private String visitorKey;

    /** 当日累计观看秒数 */
    private Long seconds;

    private LocalDate watchDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
