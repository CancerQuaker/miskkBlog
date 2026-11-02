package cn.miskk.blog.mapper;

import cn.miskk.blog.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（PostgreSQL 单库 miskk_blog）。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
