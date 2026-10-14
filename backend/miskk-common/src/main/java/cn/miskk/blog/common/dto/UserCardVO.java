package cn.miskk.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户名片（昵称 + 头像），供评论、文章作者等信息跨服务展示。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCardVO {

    private Long id;
    private String nickname;
    private String avatar;
}
