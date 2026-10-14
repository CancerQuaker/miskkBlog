package cn.miskk.blog.service;

import cn.miskk.blog.entity.Category;
import cn.miskk.blog.common.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务。
 */
public interface CategoryService {

    List<CategoryVO> listWithCount();

    List<Category> listAll();

    Category create(Category category);

    Category update(Category category);

    void delete(Long id);
}
