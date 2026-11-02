package cn.miskk.blog.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.miskk.blog.common.exception.BizException;
import cn.miskk.blog.document.ArticleDoc;
import cn.miskk.blog.document.ShareLogDoc;
import cn.miskk.blog.document.WatchRecordDoc;
import cn.miskk.blog.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 互动服务实现（MongoDB）：分享日志 / 在线观看时长（按访客+日期聚合）。
 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private static final Set<String> CHANNELS = Set.of("qq", "wechat", "copy");

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean share(Long articleId, String channel, Long userId) {
        if (channel == null || !CHANNELS.contains(channel.toLowerCase())) {
            throw new BizException("不支持的分享渠道");
        }
        ArticleDoc article = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(articleId)
                .and("deleted").ne(true)), ArticleDoc.class);
        if (article == null || article.getStatus() != 1) {
            throw new BizException("文章不存在或未发布");
        }

        ShareLogDoc log = new ShareLogDoc();
        log.setId(IdUtil.getSnowflakeNextId());
        log.setArticleId(articleId);
        log.setUserId(userId);
        log.setChannel(channel.toLowerCase());
        log.setCreatedAt(LocalDateTime.now());
        mongoTemplate.insert(log);

        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(articleId)),
                new Update().inc("shares", 1), ArticleDoc.class);
        return true;
    }

    @Override
    public void watch(Long articleId, int seconds, Long userId, String visitorKey) {
        ArticleDoc article = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(articleId)
                .and("deleted").ne(true)), ArticleDoc.class);
        if (article == null) {
            throw new BizException("文章不存在");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        Criteria identity = userId != null
                ? Criteria.where("articleId").is(articleId).and("watchDate").is(today).and("userId").is(userId)
                : Criteria.where("articleId").is(articleId).and("watchDate").is(today).and("visitorKey").is(visitorKey);
        WatchRecordDoc record = mongoTemplate.findOne(Query.query(identity).limit(1), WatchRecordDoc.class);

        if (record == null) {
            record = new WatchRecordDoc();
            record.setId(IdUtil.getSnowflakeNextId());
            record.setArticleId(articleId);
            record.setUserId(userId);
            record.setVisitorKey(userId == null ? visitorKey : null);
            record.setSeconds((long) seconds);
            record.setWatchDate(today);
            record.setCreatedAt(now);
            record.setUpdatedAt(now);
            mongoTemplate.insert(record);
        } else {
            mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(record.getId())),
                    new Update().inc("seconds", seconds).set("updatedAt", now), WatchRecordDoc.class);
        }

        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(articleId)),
                new Update().inc("watchSeconds", seconds), ArticleDoc.class);
    }
}
