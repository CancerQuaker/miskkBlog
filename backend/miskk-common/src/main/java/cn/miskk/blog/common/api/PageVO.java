package cn.miskk.blog.common.api;

import lombok.Data;

import java.util.List;

/**
 * 分页响应结构（纯传输对象，不依赖 MyBatis-Plus，供网关与下游服务共享）。
 */
@Data
public class PageVO<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public static <T> PageVO<T> empty(long page, long size) {
        PageVO<T> vo = new PageVO<>();
        vo.records = List.of();
        vo.total = 0;
        vo.page = page;
        vo.size = size;
        return vo;
    }
}
