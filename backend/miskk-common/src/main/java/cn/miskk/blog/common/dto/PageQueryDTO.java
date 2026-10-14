package cn.miskk.blog.common.dto;

import lombok.Data;

/**
 * 通用分页/筛选查询请求（POST + JSON body）。
 */
@Data
public class PageQueryDTO {

    private long page = 1;

    private long size = 10;

    private String keyword;

    private Long categoryId;

    private Long tagId;

    private Integer status;
}
