package cn.miskk.blog.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 友链申请注意事项响应。
 */
@Data
public class FriendNotesVO {

    /** 必填项 */
    private List<String> required;

    /** 选填项 */
    private List<String> optional;

    /** 审核规则说明 */
    private List<String> rules;
}
