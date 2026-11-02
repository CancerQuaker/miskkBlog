package cn.miskk.blog.service;

/**
 * 点赞服务：每个用户对每篇文章仅能点赞一次（可取消）。
 */
public interface LikeService {

    /** 点赞，重复点赞幂等返回 false */
    boolean like(Long articleId, Long userId);

    /** 取消点赞 */
    boolean unlike(Long articleId, Long userId);
}
