package cn.miskk.blog.service;

/**
 * 互动服务：分享统计。
 */
public interface InteractionService {

    /** 记录一次分享（可匿名），返回是否有效记录 */
    boolean share(Long articleId, String channel, Long userId);
}
