package cn.miskk.blog.common.constant;

/**
 * Redis 缓存 Key 约定。
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /** 邮箱验证码：%s = email */
    public static final String EMAIL_CODE = "miskk:code:email:%s";

    /** 文章浏览量缓冲（Hash：articleId -> 待落库增量） */
    public static final String ARTICLE_VIEW_BUFFER = "miskk:article:view:buf";

    /** 文章浏览去重：%s = articleId, %s = yyyyMMdd */
    public static final String ARTICLE_VIEW_DEDUP = "miskk:article:view:dedup:%s:%s";

    /** 用户名片缓存：%s = userId */
    public static final String USER_CARD = "miskk:user:card:%s";

    /** 首页文章列表缓存：%s = cacheKey */
    public static final String ARTICLE_PAGE_CACHE = "miskk:article:page:%s";

    /** OAuth state 临时存储：%s = state */
    public static final String OAUTH_STATE = "miskk:oauth:state:%s";

    public static final int CACHE_MEDIUM_TTL_SECONDS = 600;
    public static final int CACHE_SHORT_TTL_SECONDS = 120;
}
