package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 分享日志文档（MongoDB collection: share_log）。
 */
@Data
@Document("share_log")
public class ShareLogDoc {

    @Id
    private Long id;

    @Indexed
    private Long articleId;

    /** 用户 ID（匿名分享为空） */
    private Long userId;

    /** 渠道：qq/wechat/copy */
    private String channel;

    private LocalDateTime createdAt;
}
