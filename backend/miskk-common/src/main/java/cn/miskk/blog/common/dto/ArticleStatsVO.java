package cn.miskk.blog.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章服务站点统计（供管理服务聚合）。
 */
@Data
public class ArticleStatsVO {
    private Long totalArticles;
    private Long publishedArticles;
    private Long totalComments;
    private Long totalLikes;
    private Long totalViews;
    private Long totalShares;
    private Long monthPublished;
    /** 最近 30 天每日发布数 */
    private List<DailyCountVO> dailyPosts;
    /** 最近 14 天每日评论数 */
    private List<DailyCountVO> dailyComments;
    /** 浏览量 Top10 文章 */
    private List<TopArticleVO> topArticles;
}
