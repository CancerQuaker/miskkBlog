package cn.miskk.blog.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 友链申请请求。
 */
@Data
public class FriendApplyDTO {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 32, message = "站点名称最多 32 个字符")
    private String name;

    @NotBlank(message = "站点链接不能为空")
    @Size(max = 255, message = "链接过长")
    private String url;

    @Size(max = 255, message = "头像链接过长")
    private String avatar;

    @Size(max = 100, message = "站点简介最多 100 个字符")
    private String description;

    @NotBlank(message = "联系方式不能为空")
    @Size(max = 64, message = "联系方式过长")
    private String contact;

    @Size(max = 200, message = "申请说明最多 200 个字符")
    private String reason;
}
