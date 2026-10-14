package cn.miskk.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分享日志（t_share_log）：QQ / 微信 / 复制链接。
 */
@Data
@TableName("t_share_log")
public class ShareLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    /** 匿名分享时为空 */
    private Long userId;

    /** qq / wechat / copy */
    private String channel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
