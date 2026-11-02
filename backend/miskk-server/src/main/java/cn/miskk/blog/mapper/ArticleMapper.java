package cn.miskk.blog.mapper;

import cn.miskk.blog.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Article Mapper。
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
