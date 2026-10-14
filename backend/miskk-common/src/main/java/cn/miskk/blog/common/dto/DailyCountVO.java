package cn.miskk.blog.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按日统计计数（date: yyyy-MM-dd）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCountVO {

    private String date;
    private Long count;
}
