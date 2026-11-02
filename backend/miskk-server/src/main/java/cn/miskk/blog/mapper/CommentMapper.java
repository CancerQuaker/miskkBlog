package cn.miskk.blog.mapper;

import cn.miskk.blog.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Comment Mapper。
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
