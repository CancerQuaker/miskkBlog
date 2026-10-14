package cn.miskk.blog.service;

import cn.hutool.json.JSONUtil;
import cn.miskk.blog.common.constant.RedisKeys;
import cn.miskk.blog.common.dto.UserBatchDTO;
import cn.miskk.blog.common.dto.UserCardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户名片（昵称+头像）获取：优先 Redis 缓存，未命中走 Feign 批量查询并回填缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCardService {

    private final UserRemoteService userRemoteService;
    private final StringRedisTemplate redisTemplate;

    /** 服务间内部调用令牌，Feign 调用时以参数形式显式传递 */
    @Value("${miskk.internal-token:miskk-internal-token-dev}")
    private String internalToken;

    public Map<Long, UserCardVO> getCards(List<Long> ids) {
        Map<Long, UserCardVO> result = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        List<Long> distinct = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        List<Long> missing = new ArrayList<>();

        for (Long id : distinct) {
            String cached = redisTemplate.opsForValue().get(RedisKeys.USER_CARD + id);
            if (cached != null) {
                result.put(id, JSONUtil.toBean(cached, UserCardVO.class));
            } else {
                missing.add(id);
            }
        }

        if (!missing.isEmpty()) {
            try {
                UserBatchDTO batch = new UserBatchDTO();
                batch.setIds(missing);
                Map<Long, UserCardVO> fetched = userRemoteService.cards(internalToken, batch).getData();
                if (fetched != null) {
                    fetched.forEach((id, card) -> {
                        result.put(id, card);
                        redisTemplate.opsForValue().set(RedisKeys.USER_CARD + id,
                                JSONUtil.toJsonStr(card), Duration.ofMinutes(10));
                    });
                }
            } catch (Exception e) {
                log.warn("批量获取用户名片失败: {}", e.getMessage());
            }
        }
        return result;
    }
}
