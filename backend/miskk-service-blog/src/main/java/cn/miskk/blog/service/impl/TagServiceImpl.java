package cn.miskk.blog.service.impl;

import cn.miskk.blog.entity.ArticleTag;
import cn.miskk.blog.entity.Tag;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.mapper.ArticleTagMapper;
import cn.miskk.blog.mapper.TagMapper;
import cn.miskk.blog.service.TagService;
import cn.miskk.blog.common.vo.TagVO;
import cn.miskk.blog.common.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签服务实现。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<TagVO> listWithCount() {
        Map<Long, Long> counts = articleTagMapper.selectList(Wrappers.<ArticleTag>lambdaQuery())
                .stream().collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));

        return tagMapper.selectList(Wrappers.<Tag>lambdaQuery().orderByDesc(Tag::getId))
                .stream().map(t -> {
                    TagVO vo = new TagVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setColor(t.getColor());
                    vo.setArticleCount(counts.getOrDefault(t.getId(), 0L));
                    return vo;
                }).toList();
    }

    @Override
    public Tag create(Tag tag) {
        Long count = tagMapper.selectCount(Wrappers.<Tag>lambdaQuery().eq(Tag::getName, tag.getName()));
        if (count != null && count > 0) {
            throw new BizException("标签已存在");
        }
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public Tag update(Tag tag) {
        if (tagMapper.selectById(tag.getId()) == null) {
            throw new BizException("标签不存在");
        }
        tagMapper.updateById(tag);
        return tag;
    }

    @Override
    public void delete(Long id) {
        articleTagMapper.delete(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
    }
}
