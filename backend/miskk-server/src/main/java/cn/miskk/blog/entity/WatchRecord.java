package cn.miskk.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文章观看时长记录（t_watch_record），按 访客(用户或IP指纹) + 日期 聚合。
 */
@Data
@TableName("t_watch_record")
public class WatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    /** 登录用户 ID，匿名访客为空 */
    private Long userId;

    /** 匿名访客指纹（IP 哈希） */
    private String visitorKey;

    /** 当日累计观看秒数 */
    private Long seconds;

    private LocalDate watchDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
