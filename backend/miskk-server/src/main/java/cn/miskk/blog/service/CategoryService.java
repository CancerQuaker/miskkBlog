package cn.miskk.blog.service;

import cn.miskk.blog.common.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务（MongoDB category 集合）。
 */
public interface CategoryService {

    /** 全部分类 + 每个分类下的已发布文章数 */
    List<CategoryVO> listWithCount();
}
