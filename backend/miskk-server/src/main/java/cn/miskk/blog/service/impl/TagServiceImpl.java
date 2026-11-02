package cn.miskk.blog.service.impl;

import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.document.ArticleDoc;
import cn.miskk.blog.document.TagDoc;
import cn.miskk.blog.service.TagService;
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
 * 标签服务实现（MongoDB）：标签列表 + 引用文章数（未删除文章的 tags 数组展开统计）。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<TagVO> listWithCount() {
        Query tagsQuery = Query.query(Criteria.where("deleted").ne(true));
        tagsQuery.fields().include("tags");
        Map<Long, Long> counts = mongoTemplate.find(tagsQuery, ArticleDoc.class)
                .stream()
                .flatMap(a -> a.getTags() == null ? java.util.stream.Stream.<Long>empty() : a.getTags().stream())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        return mongoTemplate.find(new Query().with(Sort.by(Sort.Order.desc("_id"))), TagDoc.class)
                .stream().map(t -> {
                    TagVO vo = new TagVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setColor(t.getColor());
                    vo.setArticleCount(counts.getOrDefault(t.getId(), 0L));
                    return vo;
                }).toList();
    }
}
