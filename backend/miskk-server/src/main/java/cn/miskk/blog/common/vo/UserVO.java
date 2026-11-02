package cn.miskk.blog.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息 VO。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private String email;
    private String qq;
    private String wechat;
    private String github;
    /** 0 普通用户 1 管理员 */
    private Integer role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
