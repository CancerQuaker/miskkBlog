package cn.miskk.blog.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IPage → PageVO 转换器：PageVO 是不依赖 MyBatis-Plus 的纯传输结构，
 * 与持久层的 IPage 解耦，Service 层分页查询后经此转换再返回。
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
