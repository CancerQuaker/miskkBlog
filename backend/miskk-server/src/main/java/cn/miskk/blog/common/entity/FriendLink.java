package cn.miskk.blog.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链实体（t_friend_link），审核通过后展示。
 */
@Data
@TableName("t_friend_link")
public class FriendLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private String url;

    private String description;

    /** 0 待审核 1 已通过 2 已驳回 */
    private Integer status;

    /** 展示排序，越小越靠前 */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
