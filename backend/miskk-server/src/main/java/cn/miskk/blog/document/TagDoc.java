package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 文章标签文档（MongoDB collection: tag）；文章以 tags 数组引用其 id。
 */
@Data
@Document("tag")
public class TagDoc {

    @Id
    private Long id;

    @Indexed(unique = true)
    private String name;

    /** 展示颜色 */
    private String color;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
