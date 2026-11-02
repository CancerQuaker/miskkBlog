package cn.miskk.blog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 文章分类文档（MongoDB collection: category）。
 */
@Data
@Document("category")
public class CategoryDoc {

    @Id
    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
