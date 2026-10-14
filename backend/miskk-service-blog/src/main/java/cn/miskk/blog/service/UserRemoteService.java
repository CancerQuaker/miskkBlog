package cn.miskk.blog.service;

import cn.miskk.blog.common.api.Result;
import cn.miskk.blog.common.constant.SecurityConstants;
import cn.miskk.blog.common.dto.IdDTO;
import cn.miskk.blog.common.dto.UserBatchDTO;
import cn.miskk.blog.common.dto.UserCardVO;
import cn.miskk.blog.common.dto.UserStatsVO;
import cn.miskk.blog.common.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * 用户服务远程接口（Feign，统一 POST、不使用路径参数）：
 * 与网关的 service 层做法一致，按目标服务聚合为单一客户端，供本服务各处调用。
 * <p>
 * 请求/响应对象统一取自 common 的 DTO/VO；
 * 内部令牌以参数形式显式传递（X-Internal-Token）：调用方从配置
 * {@code miskk.internal-token} 读取后传入，由用户服务 /internal/** 的
 * InternalTokenInterceptor 校验。
 */
@FeignClient(name = "miskk-user", contextId = "blogUserClient")
public interface UserRemoteService {

    /** 批量获取用户名片（昵称+头像） */
    @PostMapping("/internal/users/batch")
    Result<Map<Long, UserCardVO>> cards(@RequestHeader(SecurityConstants.HEADER_INTERNAL_TOKEN) String token,
                                        @RequestBody UserBatchDTO dto);

    /** 获取用户公开信息 */
    @PostMapping("/internal/users")
    Result<UserVO> user(@RequestHeader(SecurityConstants.HEADER_INTERNAL_TOKEN) String token,
                        @RequestBody IdDTO dto);

    /** 用户统计（总数 / 本月新增） */
    @PostMapping("/internal/stats/overview")
    Result<UserStatsVO> userStats(@RequestHeader(SecurityConstants.HEADER_INTERNAL_TOKEN) String token);
}
