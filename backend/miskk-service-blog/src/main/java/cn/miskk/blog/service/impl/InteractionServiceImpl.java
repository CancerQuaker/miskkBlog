package cn.miskk.blog.service.impl;

import cn.miskk.blog.entity.Article;
import cn.miskk.blog.entity.ShareLog;
import cn.miskk.blog.entity.WatchRecord;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.mapper.ShareLogMapper;
import cn.miskk.blog.mapper.WatchRecordMapper;
import cn.miskk.blog.service.InteractionService;
import cn.miskk.blog.common.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 互动服务实现。
 */
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private static final Set<String> CHANNELS = Set.of("qq", "wechat", "copy");

    private final ArticleMapper articleMapper;
    private final ShareLogMapper shareLogMapper;
    private final WatchRecordMapper watchRecordMapper;

    @Override
    @Transactional
    public boolean share(Long articleId, String channel, Long userId) {
        if (channel == null || !CHANNELS.contains(channel.toLowerCase())) {
            throw new BizException("不支持的分享渠道");
        }
        Article article = articleMapper.selectById(articleId);
        if (article == null || article.getStatus() != 1) {
            throw new BizException("文章不存在或未发布");
        }

        ShareLog log = new ShareLog();
        log.setArticleId(articleId);
        log.setUserId(userId);
        log.setChannel(channel.toLowerCase());
        shareLogMapper.insert(log);

        Article update = new Article();
        update.setId(articleId);
        update.setShares((article.getShares() == null ? 0 : article.getShares()) + 1);
        articleMapper.updateById(update);
        return true;
    }

    @Override
    @Transactional
    public void watch(Long articleId, int seconds, Long userId, String visitorKey) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BizException("文章不存在");
        }

        LocalDate today = LocalDate.now();
        WatchRecord record = watchRecordMapper.selectOne(Wrappers.<WatchRecord>lambdaQuery()
                .eq(WatchRecord::getArticleId, articleId)
                .eq(WatchRecord::getWatchDate, today)
                .eq(userId != null, WatchRecord::getUserId, userId)
                .eq(userId == null, WatchRecord::getVisitorKey, visitorKey)
                .last("LIMIT 1"));

        if (record == null) {
            record = new WatchRecord();
            record.setArticleId(articleId);
            record.setUserId(userId);
            record.setVisitorKey(userId == null ? visitorKey : null);
            record.setSeconds((long) seconds);
            record.setWatchDate(today);
            watchRecordMapper.insert(record);
        } else {
            record.setSeconds(record.getSeconds() + seconds);
            watchRecordMapper.updateById(record);
        }

        Article update = new Article();
        update.setId(articleId);
        update.setWatchSeconds((article.getWatchSeconds() == null ? 0 : article.getWatchSeconds()) + seconds);
        articleMapper.updateById(update);
    }
}
