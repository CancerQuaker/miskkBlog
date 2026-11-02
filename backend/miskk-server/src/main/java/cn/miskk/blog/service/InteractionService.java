package cn.miskk.blog.service;

/**
 * 互动服务：分享统计与在线观看时长。
 */
public interface InteractionService {

    /** 记录一次分享（可匿名），返回是否有效记录 */
    boolean share(Long articleId, String channel, Long userId);

    /** 累计观看秒数（心跳上报），并按访客+日期聚合 */
    void watch(Long articleId, int seconds, Long userId, String visitorKey);
}
