package cn.miskk.blog.service;

import cn.miskk.blog.common.vo.TagVO;

import java.util.List;

/**
 * 标签服务（MongoDB tag 集合 + 文章 tags 数组）。
 */
public interface TagService {

    /** 全部标签 + 引用该标签的文章数 */
    List<TagVO> listWithCount();
}
