package cn.miskk.blog.service.impl;

import cn.miskk.blog.entity.Article;
import cn.miskk.blog.entity.Category;
import cn.miskk.blog.mapper.ArticleMapper;
import cn.miskk.blog.mapper.CategoryMapper;
import cn.miskk.blog.service.CategoryService;
import cn.miskk.blog.common.vo.CategoryVO;
import cn.miskk.blog.common.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    @Override
    public List<CategoryVO> listWithCount() {
        Map<Long, Long> counts = articleMapper.selectList(Wrappers.<Article>lambdaQuery()
                        .select(Article::getCategoryId).eq(Article::getStatus, 1)
                        .isNotNull(Article::getCategoryId))
                .stream().collect(Collectors.groupingBy(Article::getCategoryId, Collectors.counting()));

        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSort))
                .stream().map(c -> {
                    CategoryVO vo = new CategoryVO();
                    vo.setId(c.getId());
                    vo.setName(c.getName());
                    vo.setDescription(c.getDescription());
                    vo.setArticleCount(counts.getOrDefault(c.getId(), 0L));
                    return vo;
                }).toList();
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSort));
    }

    @Override
    public Category create(Category category) {
        Long count = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery().eq(Category::getName, category.getName()));
        if (count != null && count > 0) {
            throw new BizException("分类已存在");
        }
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category update(Category category) {
        if (categoryMapper.selectById(category.getId()) == null) {
            throw new BizException("分类不存在");
        }
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    public void delete(Long id) {
        Long used = articleMapper.selectCount(Wrappers.<Article>lambdaQuery().eq(Article::getCategoryId, id));
        if (used != null && used > 0) {
            throw new BizException("该分类下仍有文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
