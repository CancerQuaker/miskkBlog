package cn.miskk.blog.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（t_user）。
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号（注册时自动生成，GitHub 用户为 gh_xxx） */
    private String username;

    /** BCrypt 密码哈希，OAuth 用户为空 */
    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    private String qq;

    private String wechat;

    /** GitHub 用户名 */
    private String github;

    private String bio;

    /** 0 普通用户 1 管理员 */
    private Integer role;

    /** 0 正常 1 封禁 */
    private Integer status;

    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
