package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料请求。
 */
@Data
public class UpdateProfileDTO {

    @Size(min = 1, max = 16, message = "昵称长度为 1-16 个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Size(max = 255, message = "简介最多 255 个字符")
    private String bio;

    @Size(max = 20, message = "QQ 号格式不正确")
    private String qq;

    @Size(max = 64, message = "微信号过长")
    private String wechat;

    @Size(max = 64, message = "GitHub 用户名过长")
    private String github;

    @Size(max = 64, message = "邮箱格式不正确")
    private String email;
}
