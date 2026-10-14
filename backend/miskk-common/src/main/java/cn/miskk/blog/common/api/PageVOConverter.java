package cn.miskk.blog.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IPage → PageVO 转换器（仅下游服务使用）。
 * 与 PageVO 分离的原因：PageVO 是网关与下游共享的传输结构，
 * 若直接引用 MyBatis-Plus 的 IPage，未引入 mybatis-plus 的网关在加载 PageVO 时会
 * 抛出 NoClassDefFoundError。本类只在引入了 mybatis-plus 的服务内使用，按需加载。
 */
public final class PageVOConverter {

    private PageVOConverter() {
    }

    public static <E> PageVO<E> of(IPage<E> page) {
        PageVO<E> vo = new PageVO<>();
        vo.setRecords(page.getRecords());
        vo.setTotal(page.getTotal());
        vo.setPage(page.getCurrent());
        vo.setSize(page.getSize());
        return vo;
    }

    /**
     * 实体分页转换为 VO 分页。
     */
    public static <E, V> PageVO<V> of(IPage<E> page, Function<E, V> converter) {
        PageVO<V> vo = new PageVO<>();
        vo.setRecords(page.getRecords().stream().map(converter).collect(Collectors.toList()));
        vo.setTotal(page.getTotal());
        vo.setPage(page.getCurrent());
        vo.setSize(page.getSize());
        return vo;
    }
}
