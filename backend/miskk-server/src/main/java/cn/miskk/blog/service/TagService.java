package cn.miskk.blog.service;

import cn.miskk.blog.entity.Tag;
import cn.miskk.blog.common.vo.TagVO;

import java.util.List;

/**
 * 标签服务。
 */
public interface TagService {

    List<TagVO> listWithCount();

    Tag create(Tag tag);

    Tag update(Tag tag);

    void delete(Long id);
}
