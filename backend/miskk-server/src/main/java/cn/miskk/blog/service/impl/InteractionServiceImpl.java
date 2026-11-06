package cn.miskk.blog.service.impl;

import cn.miskk.blog.entity.Article;
import cn.miskk.blog.entity.ShareLog;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.mapper.ShareLogMapper;
import cn.miskk.blog.service.InteractionService;
import cn.miskk.blog.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
