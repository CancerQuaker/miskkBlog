package cn.miskk.blog.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链申请（t_friend_apply）。
 */
@Data
@TableName("t_friend_apply")
public class FriendApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatar;

    private String url;

    private String description;

    /** 联系方式（QQ/邮箱等） */
    private String contact;

    private String reason;

    /** 0 待审核 1 通过 2 驳回 */
    private Integer status;

    private String reviewNote;

    private LocalDateTime reviewedAt;

    /** 审核通过后生成的友链 ID */
    private Long linkId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
