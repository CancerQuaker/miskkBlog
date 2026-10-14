package cn.miskk.blog.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.miskk.blog.common.dto.ArticleSaveDTO;
import cn.miskk.blog.entity.Article;
import cn.miskk.blog.entity.ArticleLike;
import cn.miskk.blog.entity.ArticleTag;
import cn.miskk.blog.entity.Category;
import cn.miskk.blog.entity.Comment;
import cn.miskk.blog.entity.ShareLog;
import cn.miskk.blog.entity.Tag;
import cn.miskk.blog.mapper.ArticleLikeMapper;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.mapper.ArticleTagMapper;
import cn.miskk.blog.mapper.CategoryMapper;
import cn.miskk.blog.mapper.CommentMapper;
import cn.miskk.blog.mapper.ShareLogMapper;
import cn.miskk.blog.mapper.TagMapper;
import cn.miskk.blog.service.ArticleService;
import cn.miskk.blog.service.UserCardService;
import cn.miskk.blog.common.vo.ArticleCardVO;
import cn.miskk.blog.common.vo.ArticleDetailVO;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.common.api.PageVO;
import cn.miskk.blog.common.api.PageVOConverter;
import cn.miskk.blog.common.auth.LoginUser;
import cn.miskk.blog.common.constant.RedisKeys;
import cn.miskk.blog.common.dto.ArticleStatsVO;
import cn.miskk.blog.common.dto.DailyCountVO;
import cn.miskk.blog.common.dto.TopArticleVO;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.stream.Collectors;

/**
 * 文章服务实现：列表/详情/搜索/管理 CRUD/浏览量 Redis 缓冲/站点统计。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CommentMapper commentMapper;
    private final ArticleLikeMapper likeMapper;
    private final ShareLogMapper shareLogMapper;
    private final UserCardService userCardService;
    private final StringRedisTemplate redisTemplate;

    // ==================== 查询 ====================

    @Override
    public PageVO<ArticleCardVO> page(long page, long size, Long categoryId, Long tagId,
                                      String keyword, boolean publishedOnly) {
        Page<Article> p = new Page<>(page, Math.min(size, 50));
        var wrapper = Wrappers.<Article>lambdaQuery()
                .eq(publishedOnly, Article::getStatus, 1)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(Article::getTitle, keyword)
                        .or().like(Article::getSummary, keyword))
                .orderByDesc(Article::getPinned)
                .orderByDesc(Article::getPublishedAt)
                .orderByDesc(Article::getId);

        if (tagId != null) {
            List<Long> articleIds = articleTagMapper.selectList(Wrappers.<ArticleTag>lambdaQuery()
                            .eq(ArticleTag::getTagId, tagId))
                    .stream().map(ArticleTag::getArticleId).toList();
            if (articleIds.isEmpty()) {
                return PageVO.empty(page, size);
            }
            wrapper.in(Article::getId, articleIds);
        }

        Page<Article> result = articleMapper.selectPage(p, wrapper);
        return PageVOConverter.of(result, this::toCard);
    }

    @Override
    public ArticleDetailVO detail(Long id, LoginUser login) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        boolean admin = login != null && login.isAdmin();
        boolean owner = login != null && login.getUserId().equals(article.getAuthorId());
        if (article.getStatus() != 1 && !admin && !owner) {
            throw new BizException("文章不存在或未发布");
        }

        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtils.copyProperties(article, vo);
        vo.setPinned(article.getPinned() != null && article.getPinned() == 1);
        vo.setWordCount((long) (article.getContent() == null ? 0 : article.getContent().length()));
        vo.setReadingMinutes(Math.max(1, vo.getWordCount() / 400));

        vo.setCategoryName(categoryName(article.getCategoryId()));
        vo.setTags(findTagsOf(id));
        Map<Long, UserCardVO> cards = userCardService.getCards(List.of(article.getAuthorId()));
        vo.setAuthor(cards.get(article.getAuthorId()));

        if (login != null) {
            vo.setLiked(likeMapper.selectCount(Wrappers.<ArticleLike>lambdaQuery()
                    .eq(ArticleLike::getArticleId, id)
                    .eq(ArticleLike::getUserId, login.getUserId())) > 0);
        }

        Article prev = articleMapper.selectOne(Wrappers.<Article>lambdaQuery()
                .eq(Article::getStatus, 1)
                .lt(Article::getPublishedAt, article.getPublishedAt())
                .orderByDesc(Article::getPublishedAt).last("LIMIT 1"));
        Article next = articleMapper.selectOne(Wrappers.<Article>lambdaQuery()
                .eq(Article::getStatus, 1)
                .gt(Article::getPublishedAt, article.getPublishedAt())
                .orderByAsc(Article::getPublishedAt).last("LIMIT 1"));
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
        Page<Article> p = new Page<>(page, Math.min(size, 50));
        Page<Article> result = articleMapper.selectPage(p, Wrappers.<Article>lambdaQuery()
                .eq(Article::getStatus, 1)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Article::getTitle, keyword)
                        .or().like(Article::getSummary, keyword)
                        .or().like(Article::getContent, keyword))
                .orderByDesc(Article::getPublishedAt));
        return PageVOConverter.of(result, this::toCard);
    }

    // ==================== 管理 ====================

    @Override
    @Transactional
    public ArticleDetailVO save(ArticleSaveDTO dto, Long authorId, Long articleId) {
        Article article = articleId == null ? new Article() : articleMapper.selectById(articleId);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        if (article.getId() == null) {
            article.setViews(0L);
            article.setLikes(0L);
            article.setComments(0L);
            article.setShares(0L);
            article.setWatchSeconds(0L);
            article.setAuthorId(authorId);
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
            article.setPublishedAt(LocalDateTime.now());
        }
        if (article.getId() == null) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
        }

        // 重建标签关联
        articleTagMapper.delete(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getArticleId, article.getId()));
        if (dto.getTagNames() != null) {
            for (String name : dto.getTagNames().stream().filter(StringUtils::hasText).distinct().toList()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(upsertTag(name));
                articleTagMapper.insert(at);
            }
        }
        // 保存者视角（管理员权限）返回详情，草稿也能回显
        return detail(article.getId(), new cn.miskk.blog.common.auth.LoginUser(authorId, 1));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        articleMapper.deleteById(id);
        articleTagMapper.delete(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getArticleId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException("文章不存在");
        }
        article.setStatus(status);
        if (status == 1 && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
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
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                Article update = new Article();
                update.setId(articleId);
                update.setViews((article.getViews() == null ? 0 : article.getViews()) + delta);
                articleMapper.updateById(update);
            }
            redisTemplate.opsForHash().delete(RedisKeys.ARTICLE_VIEW_BUFFER, entry.getKey());
        }
    }

    // ==================== 站点统计 ====================

    @Override
    public ArticleStatsVO statsOverview() {
        ArticleStatsVO vo = new ArticleStatsVO();
        vo.setTotalArticles(articleMapper.selectCount(Wrappers.<Article>lambdaQuery()));
        vo.setPublishedArticles(articleMapper.selectCount(Wrappers.<Article>lambdaQuery()
                .eq(Article::getStatus, 1)));

        Long totalViews = articleMapper.selectList(Wrappers.<Article>lambdaQuery().select(Article::getViews))
                .stream().mapToLong(a -> a.getViews() == null ? 0 : a.getViews()).sum();
        vo.setTotalViews(totalViews);

        vo.setTotalComments(commentMapper.selectCount(Wrappers.<Comment>lambdaQuery()));
        vo.setTotalLikes(likeMapper.selectCount(null));
        vo.setTotalShares(shareLogMapper.selectCount(null));

        YearMonth now = YearMonth.now();
        vo.setMonthPublished(articleMapper.selectCount(Wrappers.<Article>lambdaQuery()
                .eq(Article::getStatus, 1)
                .ge(Article::getPublishedAt, now.atDay(1).atStartOfDay())
                .lt(Article::getPublishedAt, now.plusMonths(1).atDay(1).atStartOfDay())));

        Map<LocalDate, Long> postCounts = new HashMap<>();
        for (Article a : articleMapper.selectList(Wrappers.<Article>lambdaQuery()
                .select(Article::getPublishedAt).eq(Article::getStatus, 1)
                .ge(Article::getPublishedAt, LocalDate.now().minusDays(29).atStartOfDay()))) {
            if (a.getPublishedAt() != null) {
                postCounts.merge(a.getPublishedAt().toLocalDate(), 1L, Long::sum);
            }
        }
        vo.setDailyPosts(toDailyList(postCounts, 30));

        Map<LocalDate, Long> commentCounts = new HashMap<>();
        for (Comment c : commentMapper.selectList(Wrappers.<Comment>lambdaQuery()
                .select(Comment::getCreatedAt)
                .ge(Comment::getCreatedAt, LocalDate.now().minusDays(13).atStartOfDay()))) {
            if (c.getCreatedAt() != null) {
                commentCounts.merge(c.getCreatedAt().toLocalDate(), 1L, Long::sum);
            }
        }
        vo.setDailyComments(toDailyList(commentCounts, 14));

        vo.setTopArticles(articleMapper.selectList(Wrappers.<Article>lambdaQuery()
                        .eq(Article::getStatus, 1)
                        .orderByDesc(Article::getViews).last("LIMIT 10"))
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

    private ArticleCardVO toCard(Article article) {
        ArticleCardVO vo = new ArticleCardVO();
        BeanUtils.copyProperties(article, vo);
        vo.setPinned(article.getPinned() != null && article.getPinned() == 1);
        vo.setCategoryName(categoryName(article.getCategoryId()));
        vo.setTags(findTagsOf(article.getId()));
        Map<Long, UserCardVO> cards = userCardService.getCards(List.of(article.getAuthorId()));
        vo.setAuthor(cards.get(article.getAuthorId()));
        return vo;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }
        Category category = categoryMapper.selectById(categoryId);
        return category == null ? "未分类" : category.getName();
    }

    private List<TagVO> findTagsOf(Long articleId) {
        List<Long> tagIds = articleTagMapper.selectList(Wrappers.<ArticleTag>lambdaQuery()
                        .eq(ArticleTag::getArticleId, articleId))
                .stream().map(ArticleTag::getTagId).toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectBatchIds(tagIds).stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setColor(t.getColor());
            return vo;
        }).collect(Collectors.toList());
    }

    private Long upsertTag(String name) {
        Tag tag = tagMapper.selectOne(Wrappers.<Tag>lambdaQuery().eq(Tag::getName, name).last("LIMIT 1"));
        if (tag != null) {
            return tag.getId();
        }
        tag = new Tag();
        tag.setName(name);
        tag.setColor("#e8639a");
        tagMapper.insert(tag);
        return tag.getId();
    }

    private String plainText(String markdown) {
        return markdown == null ? "" : markdown.replaceAll("[#*`>\\[\\]\\-\\n]", "").trim();
    }
}
