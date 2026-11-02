package cn.miskk.blog.service.impl;

import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.document.ArticleDoc;
import cn.miskk.blog.document.CategoryDoc;
import cn.miskk.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现（MongoDB）：分类列表 + 已发布文章数统计（Java 侧聚合，数据量为个人博客级别）。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<CategoryVO> listWithCount() {
        // 已发布且未删除文章按分类计数
        Query countQuery = Query.query(Criteria.where("deleted").ne(true)
                .and("status").is(1)
                .and("categoryId").exists(true));
        countQuery.fields().include("categoryId");
        Map<Long, Long> counts = mongoTemplate.find(countQuery, ArticleDoc.class)
                .stream()
                .filter(a -> a.getCategoryId() != null)
                .collect(Collectors.groupingBy(ArticleDoc::getCategoryId, Collectors.counting()));

        return mongoTemplate.find(new Query().with(Sort.by(Sort.Order.asc("sort"))), CategoryDoc.class)
                .stream().map(c -> {
                    CategoryVO vo = new CategoryVO();
                    vo.setId(c.getId());
                    vo.setName(c.getName());
                    vo.setDescription(c.getDescription());
                    vo.setArticleCount(counts.getOrDefault(c.getId(), 0L));
                    return vo;
                }).toList();
    }
}
