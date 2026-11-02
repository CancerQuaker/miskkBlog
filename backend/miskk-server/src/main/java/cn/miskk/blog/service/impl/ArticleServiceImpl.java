package cn.miskk.blog.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.constant.RedisKeys;
import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.common.dto.ArticleStatsVO;
import cn.miskk.blog.common.dto.DailyCountVO;
import cn.miskk.blog.common.dto.TopArticleVO;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.exception.BizException;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.document.ArticleDoc;
import cn.miskk.blog.document.CategoryDoc;
import cn.miskk.blog.document.CommentDoc;
import cn.miskk.blog.document.TagDoc;import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.service.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文章服务实现（MongoDB）：列表/详情/搜索/管理 CRUD/浏览量 Redis 缓冲/站点统计。
 * <p>
 * 文章域文档全部存于 MongoDB（article/category/tag/comment/article_like/share_log/watch_record），
 * 作者名片仍经 UserCardService 查 MySQL 用户表 + Redis 缓存；
 * 计数字段（views/likes/comments/shares/watchSeconds）一律用 $inc 原子更新。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final MongoTemplate mongoTemplate;
    private final UserCardService userCardService;
    private final StringRedisTemplate redisTemplate;

    // ==================== 查询 ====================

    @Override
    public PageVO<ArticleCardVO> page(long page, long size, Long categoryId, Long tagId,
                                      String keyword, boolean publishedOnly) {
        int limit = (int) Math.min(size, 50);
        Criteria criteria = notDeleted();
        if (publishedOnly) {
            criteria = criteria.and("status").is(1);
        }
        if (categoryId != null) {
            criteria = criteria.and("categoryId").is(categoryId);
        }
        if (tagId != null) {
            criteria = criteria.and("tags").is(tagId);
        }
        if (StringUtils.hasText(keyword)) {
            Pattern kw = like(keyword);
            criteria = criteria.orOperator(
                    Criteria.where("title").regex(kw),
                    Criteria.where("summary").regex(kw));
        }
        return pageOf(buildPageQuery(criteria, page, limit), limit, page,
                Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("publishedAt"), Sort.Order.desc("_id")));
    }

    @Override
    public ArticleDetailVO detail(Long id, LoginUser login) {
        ArticleDoc article = findArticle(id);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        boolean admin = login != null && login.isAdmin();
        boolean owner = login != null && login.getUserId() != null
                && login.getUserId().equals(article.getAuthorId());
        if (article.getStatus() != 1 && !admin && !owner) {
            throw new BizException("文章不存在或未发布");
        }

        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtils.copyProperties(article, vo);
        vo.setPinned(article.getPinned() != null && article.getPinned() == 1);
        vo.setWordCount((long) (article.getContent() == null ? 0 : article.getContent().length()));
        vo.setReadingMinutes(Math.max(1, vo.getWordCount() / 400));

        vo.setCategoryName(categoryName(article.getCategoryId()));
        vo.setTags(findTagsOf(article.getTags()));
        Map<Long, UserCardVO> cards = userCardService.getCards(List.of(article.getAuthorId()));
        vo.setAuthor(cards.get(article.getAuthorId()));

        if (login != null && login.getUserId() != null) {
            vo.setLiked(mongoTemplate.exists(Query.query(Criteria.where("articleId").is(id)
                    .and("userId").is(login.getUserId())), "article_like"));
        }

        // 上下篇导航（相邻发布时间）
        Query prevQuery = Query.query(notDeleted().and("status").is(1)
                .and("publishedAt").lt(article.getPublishedAt()))
                .with(Sort.by(Sort.Order.desc("publishedAt"))).limit(1);
        ArticleDoc prev = mongoTemplate.findOne(prevQuery, ArticleDoc.class);
        Query nextQuery = Query.query(notDeleted().and("status").is(1)
                .and("publishedAt").gt(article.getPublishedAt()))
                .with(Sort.by(Sort.Order.asc("publishedAt"))).limit(1);
        ArticleDoc next = mongoTemplate.findOne(nextQuery, ArticleDoc.class);
        if (prev != null) {
            vo.setPrevId(prev.getId());
            vo.setPrevTitle(prev.getTitle());
        }
        if (next != null) {
            vo.setNextId(next.getId());
            vo.setNextTitle(next.getTitle());
        }
        return vo;
    }

    @Override
    public PageVO<ArticleCardVO> search(String keyword, long page, long size) {
        int limit = (int) Math.min(size, 50);
        Pattern kw = like(keyword);
        Criteria criteria = notDeleted().and("status").is(1)
                .orOperator(Criteria.where("title").regex(kw),
                        Criteria.where("summary").regex(kw),
                        Criteria.where("content").regex(kw));
        return pageOf(buildPageQuery(criteria, page, limit), limit, page,
                Sort.by(Sort.Order.desc("publishedAt")));
    }

    // ==================== 管理 ====================

    @Override
    public ArticleDetailVO save(ArticleSaveDTO dto, Long authorId, Long articleId) {
        LocalDateTime now = LocalDateTime.now();
        ArticleDoc article = articleId == null ? null : findArticle(articleId);
        boolean creating = article == null;
        if (creating) {
            article = new ArticleDoc();
            article.setId(IdUtil.getSnowflakeNextId());
            article.setViews(0L);
            article.setLikes(0L);
            article.setComments(0L);
            article.setShares(0L);
            article.setWatchSeconds(0L);
            article.setAuthorId(authorId);
            article.setDeleted(false);
            article.setCreatedAt(now);
        }
        article.setTitle(dto.getTitle());
        article.setSummary(StringUtils.hasText(dto.getSummary())
                ? dto.getSummary() : StrUtil.subPre(plainText(dto.getContent()), 120));
        article.setContent(dto.getContent());
        article.setCover(dto.getCover());
        article.setCategoryId(dto.getCategoryId());
        article.setStatus(dto.getStatus());
        article.setPinned(Boolean.TRUE.equals(dto.getPinned()) ? 1 : 0);
        if (dto.getStatus() == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(now);
        }
        article.setTags(dto.getTagNames() == null ? List.of()
                : dto.getTagNames().stream().filter(StringUtils::hasText).distinct()
                        .map(this::upsertTag).toList());
        article.setUpdatedAt(now);
        mongoTemplate.save(article);

        // 保存者视角（管理员权限）返回详情，草稿也能回显
        return detail(article.getId(), new LoginUser(authorId, 1));
    }

    @Override
    public void delete(Long id) {
        // 逻辑删除（评论与点赞记录保留，与原 MySQL @TableLogic 行为一致）
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id).and("deleted").ne(true)),
                new Update().set("deleted", true).set("updatedAt", LocalDateTime.now()), ArticleDoc.class);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ArticleDoc article = findArticle(id);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        Update update = new Update().set("status", status).set("updatedAt", LocalDateTime.now());
        if (status == 1 && article.getPublishedAt() == null) {
            update.set("publishedAt", LocalDateTime.now());
        }
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(id)), update, ArticleDoc.class);
    }

    // ==================== 浏览量（Redis 缓冲 + 定时落库） ====================

    @Override
    public boolean recordView(Long articleId, String visitorIdentity) {
        String dedupKey = String.format(RedisKeys.ARTICLE_VIEW_DEDUP, articleId,
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        Long added = redisTemplate.opsForSet().add(dedupKey, visitorIdentity);
        redisTemplate.expire(dedupKey, Duration.ofDays(2));
        if (added != null && added > 0) {
            redisTemplate.opsForHash().increment(RedisKeys.ARTICLE_VIEW_BUFFER,
                    String.valueOf(articleId), 1);
            return true;
        }
        return false;
    }

    @Override
    public void flushViews() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisKeys.ARTICLE_VIEW_BUFFER);
        if (entries.isEmpty()) {
            return;
        }
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long articleId = Long.valueOf(entry.getKey().toString());
            long delta = Long.parseLong(entry.getValue().toString());
            mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(articleId).and("deleted").ne(true)),
                    new Update().inc("views", delta), ArticleDoc.class);
            redisTemplate.opsForHash().delete(RedisKeys.ARTICLE_VIEW_BUFFER, entry.getKey());
        }
    }

    // ==================== 站点统计 ====================

    @Override
    public ArticleStatsVO statsOverview() {
        ArticleStatsVO vo = new ArticleStatsVO();
        vo.setTotalArticles(mongoTemplate.count(Query.query(notDeleted()), ArticleDoc.class));
        vo.setPublishedArticles(mongoTemplate.count(Query.query(notDeleted().and("status").is(1)), ArticleDoc.class));

        long totalViews = 0;
        Query viewsQuery = Query.query(notDeleted());
        viewsQuery.fields().include("views");
        for (ArticleDoc a : mongoTemplate.find(viewsQuery, ArticleDoc.class)) {
            totalViews += a.getViews() == null ? 0 : a.getViews();
        }
        vo.setTotalViews(totalViews);

        vo.setTotalComments(mongoTemplate.count(Query.query(commentNotDeleted()), CommentDoc.class));
        vo.setTotalLikes(countCollection("article_like"));
        vo.setTotalShares(countCollection("share_log"));

        YearMonth now = YearMonth.now();
        vo.setMonthPublished(mongoTemplate.count(Query.query(notDeleted().and("status").is(1)
                .and("publishedAt").gte(now.atDay(1).atStartOfDay())
                .lt(now.plusMonths(1).atDay(1).atStartOfDay())), ArticleDoc.class));

        Map<LocalDate, Long> postCounts = new HashMap<>();
        Query postsQuery = Query.query(notDeleted().and("status").is(1)
                .and("publishedAt").gte(LocalDate.now().minusDays(29).atStartOfDay()));
        postsQuery.fields().include("publishedAt");
        for (ArticleDoc a : mongoTemplate.find(postsQuery, ArticleDoc.class)) {
            if (a.getPublishedAt() != null) {
                postCounts.merge(a.getPublishedAt().toLocalDate(), 1L, Long::sum);
            }
        }
        vo.setDailyPosts(toDailyList(postCounts, 30));

        Map<LocalDate, Long> commentCounts = new HashMap<>();
        Query commentsQuery = Query.query(commentNotDeleted()
                .and("createdAt").gte(LocalDate.now().minusDays(13).atStartOfDay()));
        commentsQuery.fields().include("createdAt");
        for (CommentDoc c : mongoTemplate.find(commentsQuery, CommentDoc.class)) {
            if (c.getCreatedAt() != null) {
                commentCounts.merge(c.getCreatedAt().toLocalDate(), 1L, Long::sum);
            }
        }
        vo.setDailyComments(toDailyList(commentCounts, 14));

        vo.setTopArticles(mongoTemplate.find(Query.query(notDeleted().and("status").is(1))
                        .with(Sort.by(Sort.Order.desc("views"))).limit(10), ArticleDoc.class)
                .stream()
                .map(a -> new TopArticleVO(a.getId(), a.getTitle(), a.getViews(), a.getLikes(), a.getComments()))
                .toList());
        return vo;
    }

    private List<DailyCountVO> toDailyList(Map<LocalDate, Long> counts, int days) {
        List<DailyCountVO> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            list.add(new DailyCountVO(d.toString(), counts.getOrDefault(d, 0L)));
        }
        return list;
    }

    // ==================== 私有工具 ====================

    /** 未逻辑删除的文章条件 */
    private Criteria notDeleted() {
        return Criteria.where("deleted").ne(true);
    }

    /** 未逻辑删除的评论条件 */
    private Criteria commentNotDeleted() {
        return Criteria.where("deleted").ne(true);
    }

    /** 按 _id 取未逻辑删除的文章 */
    private ArticleDoc findArticle(Long id) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id).and("deleted").ne(true)),
                ArticleDoc.class);
    }

    private Query buildPageQuery(Criteria criteria, long page, int limit) {
        Query query = Query.query(criteria);
        query.skip((page - 1) * limit).limit(limit);
        return query;
    }

    private PageVO<ArticleCardVO> pageOf(Query query, int limit, long page, Sort sort) {
        query.with(sort);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ArticleDoc.class);
        List<ArticleCardVO> records = mongoTemplate.find(query, ArticleDoc.class)
                .stream().map(this::toCard).collect(Collectors.toList());
        PageVO<ArticleCardVO> vo = new PageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        vo.setPage(page);
        vo.setSize(limit);
        return vo;
    }

    private ArticleCardVO toCard(ArticleDoc article) {
        ArticleCardVO vo = new ArticleCardVO();
        BeanUtils.copyProperties(article, vo);
        vo.setPinned(article.getPinned() != null && article.getPinned() == 1);
        vo.setCategoryName(categoryName(article.getCategoryId()));
        vo.setTags(findTagsOf(article.getTags()));
        Map<Long, UserCardVO> cards = userCardService.getCards(List.of(article.getAuthorId()));
        vo.setAuthor(cards.get(article.getAuthorId()));
        return vo;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        CategoryDoc category = mongoTemplate.findById(categoryId, CategoryDoc.class);
        return category == null ? "未分类" : category.getName();
    }

    private List<TagVO> findTagsOf(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(tagIds)), TagDoc.class)
                .stream().map(t -> {
                    TagVO vo = new TagVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setColor(t.getColor());
                    return vo;
                }).collect(Collectors.toList());
    }

    /** 标签不存在则创建（默认色），返回标签 ID */
    private Long upsertTag(String name) {
        TagDoc tag = mongoTemplate.findOne(Query.query(Criteria.where("name").is(name)), TagDoc.class);
        if (tag != null) {
            return tag.getId();
        }
        tag = new TagDoc();
        tag.setId(IdUtil.getSnowflakeNextId());
        tag.setName(name);
        tag.setColor("#e8639a");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        try {
            mongoTemplate.insert(tag);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发下同名标签被抢先创建，取已有的
            return mongoTemplate.findOne(Query.query(Criteria.where("name").is(name)), TagDoc.class).getId();
        }
        return tag.getId();
    }

    /** LIKE '%kw%' 等价的忽略大小写正则 */
    private Pattern like(String keyword) {
        return Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
    }

    private long countCollection(String collection) {
        return mongoTemplate.count(new Query(), collection);
    }

    private String plainText(String markdown) {
        return markdown == null ? "" : markdown.replaceAll("[#*`>\\[\\]\\-\\n]", "").trim();
    }
}
