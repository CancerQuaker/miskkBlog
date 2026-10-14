package cn.miskk.blog.task;

import cn.miskk.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 浏览量缓冲定时落库任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewFlushTask {

    private final ArticleService articleService;

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void flush() {
        try {
            articleService.flushViews();
        } catch (Exception e) {
            log.warn("浏览量落库失败: {}", e.getMessage());
        }
    }
}
